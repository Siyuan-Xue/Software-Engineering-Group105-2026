package com.bupt.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashesShouldVerifyOriginalPasswordAndUseSalt() {
        String first = PasswordUtil.hashPassword("secret-password");
        String second = PasswordUtil.hashPassword("secret-password");

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.checkPassword("secret-password", first));
        assertFalse(PasswordUtil.checkPassword("wrong-password", first));
    }

    @Test
    void invalidHashShouldFailClosed() {
        assertFalse(PasswordUtil.checkPassword("secret", null));
        assertFalse(PasswordUtil.checkPassword("secret", ""));
        assertFalse(PasswordUtil.checkPassword("secret", "not-a-bcrypt-hash"));
    }
}
