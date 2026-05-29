package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.auth.LoginRequest;
import com.brewledger.brewledger.backend.dto.auth.LoginResponse;
import com.brewledger.brewledger.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}