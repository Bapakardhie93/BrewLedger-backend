package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.user.CreateUserRequest;
import com.brewledger.brewledger.backend.dto.user.RoleResponse;
import com.brewledger.brewledger.backend.dto.user.UpdateUserRequest;
import com.brewledger.brewledger.backend.dto.user.UserResponse;
import com.brewledger.brewledger.backend.entity.Role;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.RoleRepository;
import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role tidak ditemukan"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role tidak ditemukan"));

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setRole(role);

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getUsername());
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        userRepository.delete(user);
        log.info("User deleted with id: {}", id);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }

    private UserResponse mapToResponse(User user) {
        RoleResponse roleResponse = new RoleResponse(
                user.getRole().getId(),
                user.getRole().getName(),
                user.getRole().getDescription()
        );

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getActive(),
                user.getMustChangePassword(),
                user.getLastLogin(),
                roleResponse
        );
    }
}
