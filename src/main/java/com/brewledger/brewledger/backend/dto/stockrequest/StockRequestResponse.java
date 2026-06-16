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

    private String requestedByUsername;

    private String requestedByRole;

    private String targetRole;

    private String type;

    private LocalDateTime requestedAt;

    private String processedByName;

    private String processedByUsername;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    private String rejectReason;
}
