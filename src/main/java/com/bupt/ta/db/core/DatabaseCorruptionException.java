package com.bupt.ta.db.core;

/**
 * Raised when a JSON table cannot be parsed or validated safely.
 */
public class DatabaseCorruptionException extends DatabaseException {
    public DatabaseCorruptionException(String message) {
        super(message);
    }

    public DatabaseCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
