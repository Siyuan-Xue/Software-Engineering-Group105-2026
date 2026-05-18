package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.DatabaseSeeder;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaAccountServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void registerTaShouldCreateTaUserWithHashedPassword() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        TaAccountService svc = new TaAccountService(db);
        String email = "new-ta-" + UUID.randomUUID() + "@example.com";

        svc.registerTa(email, "password12", "password12", "New TA User", null, null, null);

        User saved = db.users().findByEmail(email).orElseThrow();
        assertEquals(UserRole.TA, saved.getRole());
        assertTrue(PasswordUtil.checkPassword("password12", saved.getPasswordHash()));
    }

    @Test
    void registerTaShouldRejectDuplicateEmail() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        TaAccountService svc = new TaAccountService(db);

        assertThrows(ConstraintViolationException.class, () ->
                svc.registerTa(DatabaseSeeder.DEFAULT_TA_EMAIL, "password12", "password12", "Dup", null, null, null));
    }

    @Test
    void registerTaShouldRejectPasswordMismatch() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        TaAccountService svc = new TaAccountService(db);

        assertThrows(ConstraintViolationException.class, () ->
                svc.registerTa("a@b.com", "password12", "password99", "Name", null, null, null));
    }

    @Test
    void resetPasswordForTaShouldUpdateTaOnly() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        TaAccountService svc = new TaAccountService(db);

        assertTrue(svc.resetPasswordForTa(DatabaseSeeder.DEFAULT_TA_EMAIL, "newpass99", "newpass99"));
        User ta = db.users().findByEmail(DatabaseSeeder.DEFAULT_TA_EMAIL).orElseThrow();
        assertTrue(PasswordUtil.checkPassword("newpass99", ta.getPasswordHash()));

        assertFalse(svc.resetPasswordForTa(DatabaseSeeder.DEFAULT_MO_EMAIL, "newpass99", "newpass99"));
        assertFalse(svc.resetPasswordForTa("nobody-" + UUID.randomUUID() + "@example.com", "newpass99", "newpass99"));
    }
}
