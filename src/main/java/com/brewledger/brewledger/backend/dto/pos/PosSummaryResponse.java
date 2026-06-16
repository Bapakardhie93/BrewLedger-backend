package com.brewledger.brewledger.backend.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosSummaryResponse {
    private Boolean shiftActive;
    private Long activeShiftId;
    private Long todaySalesCount;
    private Double todaySalesAmount;
    private Long pendingKitchenOrdersCount;
}
