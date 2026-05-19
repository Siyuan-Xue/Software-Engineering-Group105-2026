package com.bupt.ta.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing utilities backed by BCrypt.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password for registration and password reset flows.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(hashPassword("123456"));
    }
}
