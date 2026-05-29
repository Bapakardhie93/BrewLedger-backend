package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}