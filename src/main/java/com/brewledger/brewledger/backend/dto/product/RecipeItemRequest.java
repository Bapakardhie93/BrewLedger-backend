package com.brewledger.brewledger.backend.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeItemRequest {

    @NotNull(message = "ID bahan baku wajib diisi")
    private Long ingredientId;

    @NotNull(message = "Jumlah pemakaian wajib diisi")
    @Positive(message = "Jumlah pemakaian harus lebih besar dari 0")
    private Double quantityRequired;
}
