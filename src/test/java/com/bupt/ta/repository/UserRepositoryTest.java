package com.bupt.ta.repository;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.DataAccessException;
import com.bupt.ta.persistence.json.JsonUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveShouldNormalizeAndFindByEmail() {
        UserRepository repository = new JsonUserRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        User user = new User();
        user.setEmail("  User@Example.com ");
        user.setPasswordHash("hash");
        user.setRole(UserRole.TA);
        user.setFullName("Siyuan Xue");

        User saved = repository.save(user);

        assertEquals("user@example.com", saved.getEmail());
        assertTrue(repository.findByEmail("USER@example.com").isPresent());
    }

    @Test
    void saveShouldRejectDuplicateEmailIgnoringCase() {
        UserRepository repository = new JsonUserRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        User first = new User();
        first.setEmail("user@example.com");
        first.setPasswordHash("hash");
        first.setRole(UserRole.TA);
        first.setFullName("First");
        repository.save(first);

        User duplicate = new User();
        duplicate.setEmail("USER@example.com");
        duplicate.setPasswordHash("hash2");
        duplicate.setRole(UserRole.MO);
        duplicate.setFullName("Second");

        assertThrows(DataAccessException.class, () -> repository.save(duplicate));
    }

    @Test
    void setActiveShouldPersistStateChange() {
        UserRepository repository = new JsonUserRepository(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setFullName("Admin");
        User saved = repository.save(user);

        assertTrue(repository.setActive(saved.getId(), false));
        assertFalse(repository.findById(saved.getId()).orElseThrow().isActive());
    }
}
