package com.bupt.ta.db.core;

/**
 * Base unchecked exception for the JSON persistence layer.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
