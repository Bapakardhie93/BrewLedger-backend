package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.Role;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.repository.RoleRepository;
import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Order(2)
@Slf4j
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${brewledger.admin.full-name}")
    private String adminFullName;

    @Value("${brewledger.admin.username}")
    private String adminUsername;

    @Value("${brewledger.admin.password}")
    private String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        Role adminRole = roleRepository.findByName("MANAGEMENT")
                .orElseThrow(() ->
                        new RuntimeException("Role MANAGEMENT tidak ditemukan"));

        User admin = new User();

        admin.setFullName(adminFullName);
        admin.setUsername(adminUsername);
        admin.setPassword(
                passwordEncoder.encode(adminPassword)
        );

        admin.setActive(true);
        admin.setMustChangePassword(true);
        admin.setRole(adminRole);

        userRepository.save(admin);

        log.info("Default admin created with username: {}", adminUsername);
    }
}