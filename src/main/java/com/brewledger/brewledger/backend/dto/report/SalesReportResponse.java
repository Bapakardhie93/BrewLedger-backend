package com.brewledger.brewledger.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class SalesReportResponse {
    private Double totalSalesAmount;
    private Long totalTransactions;
    private Double averageTransactionValue;
    private Double taxAmount;
    private List<DailySalesSummary> dailySales;
    private List<SalesByProductSummary> salesByProduct;

    @Getter
    @AllArgsConstructor
    public static class DailySalesSummary {
        private LocalDate date;
        private Double totalSales;
        private Long transactionCount;
    }

    @Getter
    @AllArgsConstructor
    public static class SalesByProductSummary {
        private String productName;
        private Long quantitySold;
        private Double totalRevenue;
    }
}
