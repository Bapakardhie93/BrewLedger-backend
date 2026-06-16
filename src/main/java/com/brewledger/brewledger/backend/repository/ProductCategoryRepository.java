package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}