package com.bupt.ta.persistence.json;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.JsonTableStore;
import com.bupt.ta.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class JsonUserRepository implements UserRepository {
    private static final String FILE_NAME = "users.json";

    private final JsonTableStore<User> store;

    public JsonUserRepository(DatabaseConfig config) {
        this.store = new JsonTableStore<>(config, FILE_NAME, User.class);
        this.store.initialize();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return store.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = normalizeEmail(email);
        return store.findAll().stream()
                .filter(user -> normalizedEmail.equals(normalizeEmail(user.getEmail())))
                .findFirst();
    }

    @Override
    public List<User> listAll() {
        return store.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public User save(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new DataAccessException("User email must not be blank");
        }

        String normalizedEmail = normalizeEmail(user.getEmail());
        Optional<User> existing = findByEmail(normalizedEmail);
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            throw new DataAccessException("Duplicate email: " + user.getEmail());
        }

        user.setEmail(normalizedEmail);
        return store.save(user);
    }

    @Override
    public boolean setActive(UUID userId, boolean active) {
        Optional<User> existing = store.findById(userId);
        if (existing.isEmpty()) {
            return false;
        }

        User user = existing.get();
        user.setActive(active);
        store.save(user);
        return true;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
