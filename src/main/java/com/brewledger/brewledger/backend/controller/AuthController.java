package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.auth.LoginRequest;
import com.brewledger.brewledger.backend.dto.auth.LoginResponse;
import com.brewledger.brewledger.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials (username + password)
     * @return HTTP 200 with LoginResponse, or 401 if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}