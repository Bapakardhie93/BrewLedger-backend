package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {
}