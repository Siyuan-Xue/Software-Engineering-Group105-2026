package com.bupt.ta.db.core;

/**
 * Raised when domain or repository validation rejects a requested change.
 */
public class ConstraintViolationException extends DatabaseException {
    public ConstraintViolationException(String message) {
        super(message);
    }
}
