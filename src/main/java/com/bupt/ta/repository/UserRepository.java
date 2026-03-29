package com.bupt.ta.repository;

import com.bupt.ta.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> listAll();

    User save(User user);

    boolean setActive(UUID userId, boolean active);
}
