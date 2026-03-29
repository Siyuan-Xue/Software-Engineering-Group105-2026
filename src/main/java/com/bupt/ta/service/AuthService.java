package com.bupt.ta.service;

import com.bupt.ta.model.User;
import com.bupt.ta.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean setActive(UUID userId, boolean active) {
        return userRepository.setActive(userId, active);
    }
}
