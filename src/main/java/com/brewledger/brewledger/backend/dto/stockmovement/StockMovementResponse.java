package com.brewledger.brewledger.backend.dto.stockmovement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;

    private String ingredientName;

    private String movementType;

    private Double quantity;

    private Double stockBefore;

    private Double stockAfter;

    private String referenceNumber;

    private LocalDateTime movementDate;
}