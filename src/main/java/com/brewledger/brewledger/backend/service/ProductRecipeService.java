package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.recipe.CreateProductRecipeRequest;
import com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductRecipe;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.ProductRecipeRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductRecipeService {

    private final ProductRecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional
    public ProductRecipeResponse create(CreateProductRecipeRequest request) {

        Long productId = java.util.Objects.requireNonNull(request.getProductId(), "Product ID must not be null");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produk tidak ditemukan dengan ID: " + request.getProductId()
                ));

        Long ingredientId = java.util.Objects.requireNonNull(request.getIngredientId(), "Ingredient ID must not be null");
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + request.getIngredientId()
                ));

        boolean exists = recipeRepository.existsByProductAndIngredient(product, ingredient);

        if (exists) {
            throw new BusinessException(
                    "Recipe untuk produk '" + product.getName()
                            + "' dengan ingredient '" + ingredient.getName() + "' sudah terdaftar"
            );
        }

        ProductRecipe recipe = new ProductRecipe();
        recipe.setProduct(product);
        recipe.setIngredient(ingredient);
        recipe.setQuantityRequired(request.getQuantityRequired());

        recipeRepository.save(recipe);

        return mapToResponse(recipe);
    }

    @Transactional(readOnly = true)
    public List<ProductRecipeResponse> findAll() {

        return recipeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductRecipeResponse> findByProduct(@org.springframework.lang.NonNull Long productId) {

        return recipeRepository
                .findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProductRecipeResponse mapToResponse(ProductRecipe recipe) {

        return new ProductRecipeResponse(
                recipe.getId(),
                recipe.getProduct().getName(),
                recipe.getIngredient().getName(),
                recipe.getQuantityRequired()
        );
    }

    @Transactional(readOnly = true)
    public ProductRecipeResponse findById(Long id) {
        ProductRecipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resep tidak ditemukan dengan ID: " + id
                ));
        return mapToResponse(recipe);
    }

    @Transactional
    public ProductRecipeResponse update(Long id, com.brewledger.brewledger.backend.dto.recipe.UpdateProductRecipeRequest request) {
        ProductRecipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resep tidak ditemukan dengan ID: " + id
                ));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produk tidak ditemukan dengan ID: " + request.getProductId()
                ));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + request.getIngredientId()
                ));

        boolean exists = recipeRepository.existsByProductIdAndIngredientIdAndIdNot(
                request.getProductId(), request.getIngredientId(), id
        );

        if (exists) {
            throw new BusinessException(
                    "Recipe untuk produk '" + product.getName()
                            + "' dengan ingredient '" + ingredient.getName() + "' sudah terdaftar"
            );
        }

        recipe.setProduct(product);
        recipe.setIngredient(ingredient);
        recipe.setQuantityRequired(request.getQuantityRequired());

        return mapToResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        ProductRecipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resep tidak ditemukan dengan ID: " + id
                ));
        recipeRepository.delete(recipe);
    }
}