package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalStockResponse {

    private Long ingredientId;

    private String code;

    private String name;

    private String unit;

    private Double currentStock;

    private Double minimumStock;

    private Double shortage;
}
