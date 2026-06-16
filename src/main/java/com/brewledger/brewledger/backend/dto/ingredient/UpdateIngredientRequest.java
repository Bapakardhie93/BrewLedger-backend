package com.brewledger.brewledger.backend.dto.ingredient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateIngredientRequest {
    private String code;
    private String name;
    private Long supplierId;
    private String unit;
    private Double minimumStock;
    private Double costPrice;
    private Double purchasePrice;
    private Double packSize;
    private Boolean active;
}
