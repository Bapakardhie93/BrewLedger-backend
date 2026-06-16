package com.brewledger.brewledger.backend.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseIngredientResponse {

    private Long id;

    private String code;

    private String name;

    private String unit;

    private Double currentStock;

    private Double minimumStock;

    private Double costPrice;

    private Double purchasePrice;

    private Double packSize;

    private String supplierName;

    private String stockStatus;

    private Boolean active;
}
