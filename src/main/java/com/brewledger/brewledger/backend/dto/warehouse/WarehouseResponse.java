package com.brewledger.brewledger.backend.dto.warehouse;

import com.brewledger.brewledger.backend.dto.stockmovement.StockMovementResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class WarehouseResponse {

    private LocalDateTime generatedAt;

    private Long totalIngredients;

    private Double totalStock;

    private Long lowStockCount;

    private Long requestApprovalCount;

    private List<WarehouseIngredientResponse> inventory;

    private List<WarehouseRecipeResponse> productComposition;

    private List<StockMovementResponse> stockMovements;

    private List<PurchaseApprovalResponse> approvalRequests;
}
