package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.Role;
import com.brewledger.brewledger.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        createRoleIfNotExists(
                "ADMIN",
                "Administrator sistem"
        );

        createRoleIfNotExists(
                "MANAJEMEN",
                "Pengelolaan bisnis dan laporan"
        );

        createRoleIfNotExists(
                "GUDANG",
                "Pengelolaan stok dan bahan baku"
        );

        createRoleIfNotExists(
                "KASIR",
                "Transaksi penjualan"
        );
    }

    private void createRoleIfNotExists(String name, String description) {

        if (!roleRepository.existsByName(name)) {

            Role role = new Role();
            role.setName(name);
            role.setDescription(description);

            roleRepository.save(role);

            System.out.println("Role dibuat: " + name);
        }
    }
}