package com.brewledger.brewledger.backend.dto.recipe;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRecipeRequest {

    @NotNull(message = "Produk wajib dipilih")
    private Long productId;

    @NotNull(message = "Ingredient wajib dipilih")
    private Long ingredientId;

    @NotNull(message = "Quantity wajib diisi")
    @Positive(message = "Quantity harus lebih dari 0")
    private Double quantityRequired;
}