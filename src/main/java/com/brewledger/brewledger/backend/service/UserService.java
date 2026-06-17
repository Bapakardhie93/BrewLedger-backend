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
    private final ActivityLogService activityLogService;

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
        validateRoleConstraint(role);

        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);
        user.setMustChangePassword(true);
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        activityLogService.record("CREATE_USER", 
                "Created user: " + savedUser.getUsername() + " (role: " + savedUser.getRole().getName() + ")",
                "USER", savedUser.getId());
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
        validateRoleConstraint(role);

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setRole(role);
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getUsername());
        activityLogService.record("UPDATE_USER", 
                "Updated user: " + updatedUser.getUsername() + " (role: " + updatedUser.getRole().getName() + ")",
                "USER", updatedUser.getId());
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        userRepository.delete(user);
        log.info("User deleted with id: {}", id);
        activityLogService.record("DELETE_USER", 
                "Deleted user: " + user.getUsername(),
                "USER", user.getId());
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
        activityLogService.record("ACTIVATE_USER", 
                "Activated user: " + user.getUsername(),
                "USER", user.getId());
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
        activityLogService.record("DEACTIVATE_USER", 
                "Deactivated user: " + user.getUsername(),
                "USER", user.getId());
    }

    private void validateRoleConstraint(Role role) {
        if (role == null || !List.of("MANAGEMENT", "GUDANG", "KASIR").contains(role.getName())) {
            throw new IllegalArgumentException("Role tidak valid. Hanya role KASIR, GUDANG, dan MANAGEMENT yang diizinkan.");
        }
    }

    private UserResponse mapToResponse(User user) {
        RoleResponse roleResponse = new RoleResponse(
                user.getRole().getId(),
                user.getRole().getName(),
                user.getRole().getDescription()
        );

        boolean isOnline = user.getLastActivity() != null 
                && user.getLastActivity().isAfter(java.time.LocalDateTime.now().minusMinutes(5));

        return new UserResponse(
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

    @Transactional
    public void updateLastActivity(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan username: " + username));
        user.setLastActivity(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getOnlineUsers(String role) {
        java.time.LocalDateTime fiveMinutesAgo = java.time.LocalDateTime.now().minusMinutes(5);
        return userRepository.findAll().stream()
                .filter(u -> u.getLastActivity() != null && u.getLastActivity().isAfter(fiveMinutesAgo))
                .filter(u -> role == null || role.trim().isEmpty() || u.getRole().getName().equalsIgnoreCase(role.trim()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
