package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.util.PasswordUtil;

/**
 * Self-service onboarding for TA accounts with server-side validation.
 *
 * <p>Forgotten passwords are delegated to admins because SMTP token flows were out of scope. Error codes surfaced
 * to servlets correspond to constants below for uniform handling.</p>
 */
public final class TaAccountService {
    /** Raised when {@code email} blank. */
    public static final String ERR_EMAIL_REQUIRED = "TA_EMAIL_REQUIRED";
    /** Raised when legal name omitted. */
    public static final String ERR_FULL_NAME_REQUIRED = "TA_FULL_NAME_REQUIRED";
    /** Raised when password empty. */
    public static final String ERR_PASSWORD_REQUIRED = "TA_PASSWORD_REQUIRED";
    /** Raised when confirmation string empty. */
    public static final String ERR_CONFIRM_REQUIRED = "TA_CONFIRM_REQUIRED";
    /** Raised when password and confirmation mismatch. */
    public static final String ERR_PASSWORD_MISMATCH = "TA_PASSWORD_MISMATCH";
    /** Raised when plaintext shorter than enforced minimum ({@link #MIN_PASSWORD_LENGTH}). */
    public static final String ERR_PASSWORD_TOO_SHORT = "TA_PASSWORD_TOO_SHORT";
    /** Raised when the mailbox already occupies a stored user row. */
    public static final String ERR_EMAIL_TAKEN = "TA_EMAIL_TAKEN";

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final TaDatabase db;

    /**
     * @param db façade opened during servlet context startup
     */
    public TaAccountService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Delegates to {@link #registerUser(String, String, String, String, String, String, String, UserRole)} with TA role semantics.
     */
    public void registerTa(String email, String plainPassword, String confirmPassword, String fullName,
                           String phone, String department, String studentId) {
        registerUser(email, plainPassword, confirmPassword, fullName, phone, department, studentId, UserRole.TA);
    }

    /**
     * Persists an active TA credential record after enforcing uniqueness and password policy.
     *
     * <p>Note: the persisted {@linkplain User#setRole(UserRole) role} is always {@link UserRole#TA} for this codebase path;
     * the explicit {@code role} parameter exists solely for callers sharing the validator.</p>
     *
     * @param email                      registration mailbox (trimmed on save)
     * @param plainPassword             first password capture
     * @param confirmPassword           repeated password capture
     * @param fullName                  display/legal name required on portal profile
     * @param phone                     optional handset (blank becomes {@code null})
     * @param department                optional school text (blank becomes {@code null})
     * @param studentId                 optional enrolment identifier (blank becomes {@code null})
     * @param role                      ignored at persistence time; onboarding always emits {@link UserRole#TA}
     * @throws ConstraintViolationException with one of the {@code ERR_*} constants on validation failure
     */
    public void registerUser(String email, String plainPassword, String confirmPassword, String fullName,
                             String phone, String department, String studentId, UserRole role) {
        requireNonBlank(email, ERR_EMAIL_REQUIRED);
        requireNonBlank(fullName, ERR_FULL_NAME_REQUIRED);
        requireNonBlank(plainPassword, ERR_PASSWORD_REQUIRED);
        requireNonBlank(confirmPassword, ERR_CONFIRM_REQUIRED);
        if (!plainPassword.equals(confirmPassword)) {
            throw new ConstraintViolationException(ERR_PASSWORD_MISMATCH);
        }
        if (plainPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ConstraintViolationException(ERR_PASSWORD_TOO_SHORT);
        }

        if (db.users().findByEmail(email).isPresent()) {
            throw new ConstraintViolationException(ERR_EMAIL_TAKEN);
        }

        User user = new User();
        user.setEmail(email.trim());
        user.setFullName(fullName.trim());
        user.setRole(UserRole.TA);
        user.setActive(true);
        user.setPhone(trimToNull(phone));
        user.setDepartment(trimToNull(department));
        user.setStudentId(trimToNull(studentId));
        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword.trim()));

        db.users().save(user);
    }

    private static void requireNonBlank(String value, String errorCode) {
        if (value == null || value.isBlank()) {
            throw new ConstraintViolationException(errorCode);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
