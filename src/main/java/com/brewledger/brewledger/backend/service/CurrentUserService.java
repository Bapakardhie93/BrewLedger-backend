package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.exception.AuthException;
import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("User belum terautentikasi");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthException("User login tidak ditemukan"));
    }
}
