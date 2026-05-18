package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.util.PasswordUtil;

import java.util.Optional;

/**
 * TA-only self-service registration and password reset (no email/token infrastructure).
 */
public final class TaAccountService {
    public static final String ERR_EMAIL_REQUIRED = "TA_EMAIL_REQUIRED";
    public static final String ERR_FULL_NAME_REQUIRED = "TA_FULL_NAME_REQUIRED";
    public static final String ERR_PASSWORD_REQUIRED = "TA_PASSWORD_REQUIRED";
    public static final String ERR_CONFIRM_REQUIRED = "TA_CONFIRM_REQUIRED";
    public static final String ERR_PASSWORD_MISMATCH = "TA_PASSWORD_MISMATCH";
    public static final String ERR_PASSWORD_TOO_SHORT = "TA_PASSWORD_TOO_SHORT";
    public static final String ERR_EMAIL_TAKEN = "TA_EMAIL_TAKEN";

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final TaDatabase db;

    public TaAccountService(TaDatabase db) {
        this.db = db;
    }

    public void registerTa(String email, String plainPassword, String confirmPassword, String fullName,
                           String phone, String department, String studentId) {
        registerUser(email, plainPassword, confirmPassword, fullName, phone, department, studentId, UserRole.TA);
    }

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
        user.setRole(role == null || role == UserRole.ADMIN ? UserRole.TA : role);
        user.setActive(true);
        user.setPhone(trimToNull(phone));
        user.setDepartment(trimToNull(department));
        user.setStudentId(trimToNull(studentId));
        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword.trim()));

        db.users().save(user);
    }

    /**
     * Scheme B: if an account exists for {@code email} and its role is TA, update password and return true.
     */
    public boolean resetPasswordForTa(String email, String newPassword, String confirmPassword) {
        requireNonBlank(email, ERR_EMAIL_REQUIRED);
        requireNonBlank(newPassword, ERR_PASSWORD_REQUIRED);
        requireNonBlank(confirmPassword, ERR_CONFIRM_REQUIRED);
        if (!newPassword.equals(confirmPassword)) {
            throw new ConstraintViolationException(ERR_PASSWORD_MISMATCH);
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ConstraintViolationException(ERR_PASSWORD_TOO_SHORT);
        }

        Optional<User> found = db.users().findByEmail(email.trim());
        if (found.isEmpty() || found.get().getRole() != UserRole.TA) {
            return false;
        }
        User user = found.get();
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword.trim()));
        db.users().save(user);
        return true;
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
