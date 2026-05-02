package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class JsonUserRepository extends BaseJsonRepository<User> implements UserRepository {
    public JsonUserRepository(JsonTableStore<User> store) {
        super(store);
    }

    @Override
    public List<User> findAll() {
        return super.findAll().stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return store.query(user -> normalized.equals(normalize(user.getEmail()))).stream().findFirst();
    }

    @Override
    public List<User> listByRole(UserRole role) {
        return findAll().stream().filter(user -> user.getRole() == role).toList();
    }

    @Override
    public List<User> listActive() {
        return findAll().stream().filter(User::isActive).toList();
    }

    @Override
    public boolean setActive(UUID userId, boolean active) {
        Optional<User> existing = findById(userId);
        if (existing.isEmpty()) {
            return false;
        }
        User user = existing.get();
        user.setActive(active);
        save(user);
        return true;
    }

    @Override
    public User save(User entity) {
        if (entity.getEmail() == null || entity.getEmail().isBlank()) {
            throw new ConstraintViolationException("User email must not be blank");
        }
        if (entity.getRole() == null) {
            throw new ConstraintViolationException("User role must not be null");
        }
        if (entity.getFullName() == null || entity.getFullName().isBlank()) {
            throw new ConstraintViolationException("User fullName must not be blank");
        }
        String normalized = normalize(entity.getEmail());
        Optional<User> duplicate = findByEmail(normalized);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(entity.getId())) {
            throw new ConstraintViolationException("Duplicate email: " + entity.getEmail());
        }
        entity.setEmail(normalized);
        return super.save(entity);
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
