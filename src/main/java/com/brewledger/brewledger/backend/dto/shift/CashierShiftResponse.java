package com.brewledger.brewledger.backend.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CashierShiftResponse {
    private Long id;
    private Long cashierId;
    private String cashierName;
    private String cashierUsername;
    private String cashierRole;
    private Double openingCash;
    private Double closingCash;
    private Double expectedCash;
    private Double cashDifference;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String notes;
}
