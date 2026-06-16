package com.brewledger.brewledger.backend.dto.recipe;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRecipeRequest {
    private Long productId;
    private Long ingredientId;
    private Double quantityRequired;
}
