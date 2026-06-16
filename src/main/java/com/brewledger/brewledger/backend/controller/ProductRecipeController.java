package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.recipe.CreateProductRecipeRequest;
import com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse;
import com.brewledger.brewledger.backend.service.ProductRecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-recipes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class ProductRecipeController {

    private final ProductRecipeService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public ProductRecipeResponse create(
            @Valid
            @RequestBody CreateProductRecipeRequest request
    ) {

        return service.create(request);
    }

    @GetMapping
    public List<ProductRecipeResponse> findAll() {

        return service.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductRecipeResponse> findByProduct(
            @PathVariable Long productId
    ) {

        return service.findByProduct(productId);
    }

    @GetMapping("/{id}")
    public ProductRecipeResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public ProductRecipeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody com.brewledger.brewledger.backend.dto.recipe.UpdateProductRecipeRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
