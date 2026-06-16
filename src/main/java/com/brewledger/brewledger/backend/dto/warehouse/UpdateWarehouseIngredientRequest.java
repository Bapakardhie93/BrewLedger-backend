package com.brewledger.brewledger.backend.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWarehouseIngredientRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long supplierId;

    @NotBlank
    private String unit;

    @NotNull
    @PositiveOrZero
    private Double minimumStock;

    @NotNull
    @PositiveOrZero
    private Double costPrice;

    private Double purchasePrice;

    private Double packSize;

    @NotNull
    private Boolean active;
}
