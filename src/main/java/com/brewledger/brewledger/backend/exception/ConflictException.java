package com.brewledger.brewledger.backend.exception;

/**
 * Thrown when a request conflict occurs (e.g. invalid state transition).
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
