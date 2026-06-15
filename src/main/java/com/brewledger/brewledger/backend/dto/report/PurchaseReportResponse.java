package com.brewledger.brewledger.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseReportResponse {
    private Double totalPurchaseAmount;
    private Long totalOrders;
    private Long receivedOrders;
    private Long draftOrders;
    private List<PurchaseBySupplierSummary> purchaseBySupplier;
    private List<DailyPurchaseSummary> dailyPurchases;

    @Getter
    @AllArgsConstructor
    public static class PurchaseBySupplierSummary {
        private String supplierName;
        private Long orderCount;
        private Double totalSpent;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyPurchaseSummary {
        private LocalDate date;
        private Double totalSpent;
        private Long orderCount;
    }
}
