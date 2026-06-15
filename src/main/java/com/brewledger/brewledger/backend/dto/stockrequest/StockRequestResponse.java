package com.brewledger.brewledger.backend.dto.stockrequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockRequestResponse {

    private Long id;

    private String requestNumber;

    private Long ingredientId;

    private String ingredientCode;

    private String ingredientName;

    private String unit;

    private Double requestedQuantity;

    private String notes;

    private String status;

    private String requestedByName;

    private LocalDateTime requestedAt;

    private String processedByName;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;
}
