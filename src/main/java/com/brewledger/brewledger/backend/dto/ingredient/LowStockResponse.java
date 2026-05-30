package com.brewledger.brewledger.backend.dto.ingredient;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowStockResponse {

    private Long id;

    private String code;

    private String name;

    private Double currentStock;

    private Double minimumStock;
}