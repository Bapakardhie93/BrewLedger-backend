package com.brewledger.brewledger.backend.dto.stockrequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStockRequest {

    @NotNull(message = "Ingredient wajib dipilih")
    private Long ingredientId;

    @NotNull(message = "Jumlah permintaan wajib diisi")
    @Positive(message = "Jumlah permintaan harus lebih dari 0")
    private Double requestedQuantity;

    private String notes;
}
