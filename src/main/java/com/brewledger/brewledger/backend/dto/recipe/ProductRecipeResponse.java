package com.brewledger.brewledger.backend.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRecipeResponse {

    private Long id;

    private String productName;

    private String ingredientName;

    private Double quantityRequired;
}