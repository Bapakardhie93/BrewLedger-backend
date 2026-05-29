package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.recipe.CreateProductRecipeRequest;
import com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse;
import com.brewledger.brewledger.backend.service.ProductRecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-recipes")
@RequiredArgsConstructor
public class ProductRecipeController {

    private final ProductRecipeService service;

    @PostMapping
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
}