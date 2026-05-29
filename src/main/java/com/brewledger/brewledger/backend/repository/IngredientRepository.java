package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository
        extends JpaRepository<Ingredient, Long> {

    boolean existsByCode(String code);
}