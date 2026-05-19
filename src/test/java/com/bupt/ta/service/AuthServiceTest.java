package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void authenticateShouldRequireKnownActiveUserAndMatchingPassword() {
        TaDatabase db = TestDatabases.open(tempDir);
        AuthService service = new AuthService(db);
        User user = TestData.user(TestData.uniqueEmail("auth"), UserRole.TA, "Auth User");
        user = db.users().save(user);

        assertTrue(service.authenticate(user.getEmail().toUpperCase(), "password123").isPresent());
        assertTrue(service.authenticate("missing@example.test", "password123").isEmpty());
        assertTrue(service.authenticate(user.getEmail(), "wrong-password").isEmpty());

        assertTrue(service.setActive(user.getId(), false));
        assertTrue(service.authenticate(user.getEmail(), "password123").isEmpty());
        assertFalse(service.setActive(java.util.UUID.randomUUID(), true));
    }

    @Test
    void findByEmailShouldUseRepositoryNormalization() {
        TaDatabase db = TestDatabases.open(tempDir);
        AuthService service = new AuthService(db);
        User user = TestData.user("  " + TestData.uniqueEmail("mixed").toUpperCase() + "  ", UserRole.MO, "Mixed User");
        user = db.users().save(user);

        assertEquals(user.getId(), service.findByEmail(user.getEmail().toUpperCase()).orElseThrow().getId());
        assertTrue(service.findByEmail(" ").isEmpty());
    }
}
