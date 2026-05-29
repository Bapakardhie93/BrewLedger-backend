package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {
}