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

        // 1. Seed env admin user if configured
        if (adminUsername != null && !adminUsername.trim().isEmpty() && !userRepository.existsByUsername(adminUsername)) {
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
            log.info("Default admin from env created with username: {}", adminUsername);
        }

        // 2. Seed default admin user
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("MANAGEMENT")
                    .orElseThrow(() ->
                            new RuntimeException("Role MANAGEMENT tidak ditemukan"));
            User admin = new User();
            admin.setFullName("Default Admin");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin12345"));
            admin.setActive(true);
            admin.setMustChangePassword(true);
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("Default admin user created");
        }

        // 3. Seed default gudang user
        if (!userRepository.existsByUsername("gudang")) {
            Role role = roleRepository.findByName("GUDANG")
                    .orElseThrow(() ->
                            new RuntimeException("Role GUDANG tidak ditemukan"));
            User user = new User();
            user.setFullName("Default Gudang");
            user.setUsername("gudang");
            user.setPassword(passwordEncoder.encode("gudang12345"));
            user.setActive(true);
            user.setMustChangePassword(true);
            user.setRole(role);
            userRepository.save(user);
            log.info("Default gudang user created");
        }

        // 4. Seed default kasir user
        if (!userRepository.existsByUsername("kasir")) {
            Role role = roleRepository.findByName("KASIR")
                    .orElseThrow(() ->
                            new RuntimeException("Role KASIR tidak ditemukan"));
            User user = new User();
            user.setFullName("Default Kasir");
            user.setUsername("kasir");
            user.setPassword(passwordEncoder.encode("kasir12345"));
            user.setActive(true);
            user.setMustChangePassword(true);
            user.setRole(role);
            userRepository.save(user);
            log.info("Default kasir user created");
        }
    }
}