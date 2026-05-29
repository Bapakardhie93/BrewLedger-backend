package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.auth.LoginRequest;
import com.brewledger.brewledger.backend.dto.auth.LoginResponse;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.repository.UserRepository;
import com.brewledger.brewledger.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Username atau password salah"));

        if (!user.getActive()) {
            throw new RuntimeException("User tidak aktif");
        }

        boolean passwordMatch =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatch) {
            throw new RuntimeException("Username atau password salah");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token =
                jwtService.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().getName()
        );
    }
}