package com.brewledger.brewledger.backend.exception;

/**
 * Thrown when authentication fails (wrong credentials or user inactive).
 * Maps to HTTP 401 Unauthorized via GlobalExceptionHandler.
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
