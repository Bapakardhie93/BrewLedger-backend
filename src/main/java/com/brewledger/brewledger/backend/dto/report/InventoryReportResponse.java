package com.brewledger.brewledger.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class InventoryReportResponse {
    private Long totalIngredients;
    private Long lowStockIngredientsCount;
    private Double totalInventoryValue;
    private List<IngredientStockStatus> ingredientStockStatus;

    @Getter
    @AllArgsConstructor
    public static class IngredientStockStatus {
        private Long ingredientId;
        private String ingredientCode;
        private String ingredientName;
        private Double currentStock;
        private Double minimumStock;
        private Double costPrice;
        private Double totalValue;
        private Boolean isLowStock;
    }
}
