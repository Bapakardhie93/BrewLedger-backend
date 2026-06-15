package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class DashboardResponse {

    private LocalDateTime generatedAt;

    private Double todaySales;

    private Double salesChangePercentage;

    private Long todayTransactions;

    private Double transactionChangePercentage;

    private Long criticalStockCount;

    private Long pendingApprovalCount;

    private Long activeUsers;

    private Long totalProducts;

    private Long totalIngredients;

    private Long totalSuppliers;

    private Long totalTransactions;

    private Double totalSales;

    private Long totalStockMovements;

    private List<TopSellingProductResponse> topSellingProducts;

    private List<RecentStockMovementResponse> recentStockMovements;

    private List<RecentTransactionResponse> recentTransactions;

    private List<StockMovementSummaryResponse> topMovingIngredients;

    private List<DailyDashboardMetricResponse> lastSevenDays;

    private List<CategorySalesResponse> salesByCategory;

    private List<CriticalStockResponse> criticalStocks;
}
