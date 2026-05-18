package com.bupt.ta.db.core;

public class DatabaseCorruptionException extends DatabaseException {
    public DatabaseCorruptionException(String message) {
        super(message);
    }

    public DatabaseCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
