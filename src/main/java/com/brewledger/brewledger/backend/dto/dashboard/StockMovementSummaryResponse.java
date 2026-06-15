package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementSummaryResponse {
    private String ingredientName;
    private Double totalQuantityIn;
    private Double totalQuantityOut;
    private Double totalQuantityMoved;
    private Long movementCount;
}
