package com.eams.exception;

/**
 * Thrown by registration when the supplied email already exists in the
 * users table. Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
