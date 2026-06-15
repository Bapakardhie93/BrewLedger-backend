package com.brewledger.brewledger.backend.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseRecipeResponse {

    private Long recipeId;

    private Long productId;

    private String productCode;

    private String productName;

    private Long ingredientId;

    private String ingredientName;

    private String ingredientUnit;

    private Double quantityRequired;
}
