package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.Role;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.repository.RoleRepository;
import com.brewledger.brewledger.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Order(1)
@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleSeeder(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role managementRole = getOrCreateRole(
                "MANAGEMENT",
                "Pengelolaan penuh sistem, bisnis, user, dan laporan"
        );

        getOrCreateRole(
                "GUDANG",
                "Pengelolaan stok dan bahan baku"
        );

        getOrCreateRole(
                "KASIR",
                "Transaksi penjualan"
        );

        migrateLegacyRoles(managementRole);
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .map(role -> {
                    role.setDescription(description);
                    return roleRepository.save(role);
                })
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void migrateLegacyRoles(Role managementRole) {
        List.of("ADMIN", "MANAJEMEN").forEach(legacyRoleName ->
                roleRepository.findByName(legacyRoleName).ifPresent(legacyRole -> {
                    List<User> legacyUsers = userRepository.findAll()
                            .stream()
                            .filter(user -> user.getRole().getId().equals(legacyRole.getId()))
                            .peek(user -> user.setRole(managementRole))
                            .toList();

                    userRepository.saveAllAndFlush(legacyUsers);
                    roleRepository.delete(legacyRole);
                }));
    }
}
