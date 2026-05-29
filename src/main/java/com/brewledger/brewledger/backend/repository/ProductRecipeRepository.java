package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRecipeRepository
        extends JpaRepository<ProductRecipe, Long> {

    boolean existsByProductAndIngredient(
            Product product,
            Ingredient ingredient
    );

    List<ProductRecipe> findByProductId(
            Long productId
    );
}