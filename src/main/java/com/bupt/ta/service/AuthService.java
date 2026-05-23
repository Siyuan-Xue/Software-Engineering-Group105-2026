package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.util.PasswordUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Credential lookup helpers used by LoginServlet and administrators toggling activation flags.
 *
 * <p>This layer never mutates hashes except through delegated repository calls invoked by callers.</p>
 */
public class AuthService {
    private final TaDatabase db;

    /**
     * @param db bound JSON-backed facade for the HTTP request lifecycle
     */
    public AuthService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Finds a persisted user by mailbox address ignoring case trimming beyond repository rules.
     *
     * @param email raw login identifier
     * @return hydrated account when stored
     */
    public Optional<User> findByEmail(String email) {
        return db.users().findByEmail(email);
    }

    /**
     * Sets the {@link User#setActive(boolean)} flag for dormant account handling.
     *
     * @param userId authoritative primary key
     * @param active desired visibility in login workflows
     * @return {@code true} when the repository acknowledges the mutation
     */
    public boolean setActive(UUID userId, boolean active) {
        return db.users().setActive(userId, active);
    }

    /**
     * Verifies salted hash credentials against the stored {@link User#getPasswordHash()}.
     *
     * <p>Returns empty when email is unknown, the account is disabled, or the password mismatches.</p>
     *
     * @param email          submitted username
     * @param plainPassword  raw password captured from the servlet form (never persisted here)
     * @return authenticated user only when activation and hashing checks succeed
     */
    public Optional<User> authenticate(String email, String plainPassword) {
        Optional<User> userOpt = db.users().findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!user.isActive()) {
            return Optional.empty();
        }
        return PasswordUtil.checkPassword(plainPassword, user.getPasswordHash()) ? Optional.of(user) : Optional.empty();
    }
}
