package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentStockMovementResponse {
    private String productName;
    private String ingredientName;
    private String type;
    private Double quantity;
}
