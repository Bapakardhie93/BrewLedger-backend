package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.auth.LoginRequest;
import com.brewledger.brewledger.backend.dto.auth.LoginResponse;
import com.brewledger.brewledger.backend.dto.auth.ChangePasswordRequest;
import com.brewledger.brewledger.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }
}