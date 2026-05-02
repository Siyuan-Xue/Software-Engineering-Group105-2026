package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.util.PasswordUtil;

import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final TaDatabase db;

    public AuthService(TaDatabase db) {
        this.db = db;
    }

    public Optional<User> findByEmail(String email) {
        return db.users().findByEmail(email);
    }

    public boolean setActive(UUID userId, boolean active) {
        return db.users().setActive(userId, active);
    }

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
