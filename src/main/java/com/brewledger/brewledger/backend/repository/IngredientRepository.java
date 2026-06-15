package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository
        extends JpaRepository<Ingredient, Long> {

    boolean existsByCode(String code);

    boolean existsBySupplierId(Long supplierId);

    @Query("""
           SELECT i
           FROM Ingredient i
           WHERE i.currentStock <= i.minimumStock
           """)
    List<Ingredient> findLowStock();

    List<Ingredient> findByNameContainingIgnoreCase(
            String keyword
    );
}
