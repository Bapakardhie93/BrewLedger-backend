package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.recipe.CreateProductRecipeRequest;
import com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductRecipe;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.ProductRecipeRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRecipeService {

    private final ProductRecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    public ProductRecipeResponse create(
            CreateProductRecipeRequest request
    ) {

        Product product =
                productRepository.findById(
                        request.getProductId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Produk tidak ditemukan"
                        ));

        Ingredient ingredient =
                ingredientRepository.findById(
                        request.getIngredientId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Ingredient tidak ditemukan"
                        ));

        boolean exists =
                recipeRepository.existsByProductAndIngredient(
                        product,
                        ingredient
                );

        if (exists) {
            throw new RuntimeException(
                    "Recipe sudah terdaftar"
            );
        }

        ProductRecipe recipe =
                new ProductRecipe();

        recipe.setProduct(product);
        recipe.setIngredient(ingredient);
        recipe.setQuantityRequired(
                request.getQuantityRequired()
        );

        recipeRepository.save(recipe);

        return mapToResponse(recipe);
    }

    public List<ProductRecipeResponse> findAll() {

        return recipeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductRecipeResponse> findByProduct(
            Long productId
    ) {

        return recipeRepository
                .findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProductRecipeResponse mapToResponse(
            ProductRecipe recipe
    ) {

        return new ProductRecipeResponse(
                recipe.getId(),
                recipe.getProduct().getName(),
                recipe.getIngredient().getName(),
                recipe.getQuantityRequired()
        );
    }
}