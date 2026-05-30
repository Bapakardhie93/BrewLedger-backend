package com.brewledger.brewledger.backend.exception;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 422 Unprocessable Entity via GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
