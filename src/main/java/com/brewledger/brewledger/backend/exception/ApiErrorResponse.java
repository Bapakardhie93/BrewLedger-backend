package com.brewledger.brewledger.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private boolean success;
    private String message;
}