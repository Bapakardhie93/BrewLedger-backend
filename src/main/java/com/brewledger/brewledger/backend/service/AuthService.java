package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.auth.LoginRequest;
import com.brewledger.brewledger.backend.dto.auth.LoginResponse;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.exception.AuthException;
import com.brewledger.brewledger.backend.repository.UserRepository;
import com.brewledger.brewledger.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    /**
     * Authenticates a user, updates last login timestamp, and returns a JWT token.
     *
     * @param request the login credentials
     * @return a LoginResponse containing the JWT token, username, and role
     * @throws AuthException if credentials are invalid or the user is inactive
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Username atau password salah"));

        if (!user.getActive()) {
            log.warn("Login attempt for inactive user: {}", request.getUsername());
            throw new AuthException("Akun Anda tidak aktif. Silakan hubungi administrator.");
        }

        boolean passwordMatch =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatch) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new AuthException("Username atau password salah");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().getName());

        log.info("User '{}' logged in successfully", user.getUsername());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().getName()
        );
    }

    public void changePassword(com.brewledger.brewledger.backend.dto.auth.ChangePasswordRequest request) {
        User currentUser = currentUserService.requireCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new com.brewledger.brewledger.backend.exception.BusinessException("Password lama salah");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        currentUser.setMustChangePassword(false);
        userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getUsername());
    }

    public com.brewledger.brewledger.backend.dto.user.UserResponse getCurrentUserProfile() {
        User user = currentUserService.requireCurrentUser();
        com.brewledger.brewledger.backend.dto.user.RoleResponse roleResponse = new com.brewledger.brewledger.backend.dto.user.RoleResponse(
                user.getRole().getId(),
                user.getRole().getName(),
                user.getRole().getDescription()
        );
        boolean isOnline = user.getLastActivity() != null 
                && user.getLastActivity().isAfter(java.time.LocalDateTime.now().minusMinutes(5));
        return new com.brewledger.brewledger.backend.dto.user.UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getActive(),
                user.getMustChangePassword(),
                user.getLastLogin(),
                user.getPhoneNumber(),
                user.getLastActivity(),
                isOnline,
                roleResponse
        );
    }
}