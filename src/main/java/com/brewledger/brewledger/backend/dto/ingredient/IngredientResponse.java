package com.brewledger.brewledger.backend.dto.ingredient;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngredientResponse {

    private Long id;
    private String code;
    private String name;
    private String supplierName;
    private String unit;
    private Double currentStock;
    private Double minimumStock;
    private Double costPrice;
    private Boolean active;
}