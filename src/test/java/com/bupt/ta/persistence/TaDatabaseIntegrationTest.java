package com.bupt.ta.persistence;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaDatabaseIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void openShouldInitializeAllSprintOneTableFiles() {
        TaDatabase database = TaDatabase.open(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        assertTrue(Files.exists(tempDir.resolve("users.json")));
        assertTrue(Files.exists(tempDir.resolve("resumes.json")));
        assertTrue(Files.exists(tempDir.resolve("jobs.json")));
        assertTrue(Files.exists(tempDir.resolve("applications.json")));
        assertEquals(0, database.users().listAll().size());
    }

    @Test
    void repositoriesExposedByFacadeShouldBeUsable() {
        TaDatabase database = TaDatabase.open(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));

        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setFullName("Admin");
        database.users().save(user);

        assertTrue(database.users().findByEmail("admin@example.com").isPresent());
    }
}
