package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardResponse {

    private Long totalProducts;

    private Long totalIngredients;

    private Long totalSuppliers;

    private Long totalTransactions;

    private Double totalSales;

    private Long totalStockMovements;
}
