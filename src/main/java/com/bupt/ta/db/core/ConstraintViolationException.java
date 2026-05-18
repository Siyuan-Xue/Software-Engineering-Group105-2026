package com.bupt.ta.db.core;

public class ConstraintViolationException extends DatabaseException {
    public ConstraintViolationException(String message) {
        super(message);
    }
}
