package com.brewledger.brewledger.backend.exception;

/**
 * Thrown when a request parameter or validation mismatch occurs.
 * Maps to HTTP 400 Bad Request via GlobalExceptionHandler.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
