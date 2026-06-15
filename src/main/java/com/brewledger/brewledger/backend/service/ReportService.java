package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.report.*;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);

        List<Transaction> transactions = transactionRepository.findByDateRange(start, end);
        List<TransactionItem> items = transactionItemRepository.findByTransactionDateRange(start, end);

        double totalSalesAmount = 0.0;
        double taxAmount = 0.0;
        for (Transaction t : transactions) {
            totalSalesAmount += t.getTotal() != null ? t.getTotal() : 0.0;
            taxAmount += t.getTax() != null ? t.getTax() : 0.0;
        }

        long totalTransactions = transactions.size();
        double averageTransactionValue = totalTransactions > 0 ? totalSalesAmount / totalTransactions : 0.0;

        // Daily breakdown
        Map<LocalDate, DailySalesData> dailyMap = new HashMap<>();
        // Pre-fill daily slots
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyMap.put(current, new DailySalesData(0.0, 0L));
            current = current.plusDays(1);
        }

        for (Transaction t : transactions) {
            LocalDate date = t.getCreatedAt().toLocalDate();
            DailySalesData data = dailyMap.get(date);
            if (data == null) {
                data = new DailySalesData(0.0, 0L);
                dailyMap.put(date, data);
            }
            data.totalSales += t.getTotal() != null ? t.getTotal() : 0.0;
            data.count++;
        }

        List<SalesReportResponse.DailySalesSummary> dailySales = dailyMap.entrySet().stream()
                .map(e -> new SalesReportResponse.DailySalesSummary(e.getKey(), e.getValue().totalSales, e.getValue().count))
                .sorted(Comparator.comparing(SalesReportResponse.DailySalesSummary::getDate))
                .toList();

        // Product breakdown
        Map<String, ProductSalesData> productMap = new HashMap<>();
        for (TransactionItem item : items) {
            String name = item.getProductName();
            ProductSalesData data = productMap.get(name);
            if (data == null) {
                data = new ProductSalesData(0L, 0.0);
                productMap.put(name, data);
            }
            data.quantity += item.getQuantity() != null ? item.getQuantity() : 0;
            data.revenue += item.getSubtotal() != null ? item.getSubtotal() : 0.0;
        }

        List<SalesReportResponse.SalesByProductSummary> salesByProduct = productMap.entrySet().stream()
                .map(e -> new SalesReportResponse.SalesByProductSummary(e.getKey(), e.getValue().quantity, e.getValue().revenue))
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .toList();

        return new SalesReportResponse(
                totalSalesAmount,
                totalTransactions,
                averageTransactionValue,
                taxAmount,
                dailySales,
                salesByProduct
        );
    }

    @Transactional(readOnly = true)
    public PurchaseReportResponse getPurchaseReport(LocalDate startDate, LocalDate endDate) {
        List<PurchaseOrder> pos = purchaseOrderRepository.findByDateRange(startDate, endDate);
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderDateRange(startDate, endDate);

        double totalPurchaseAmount = 0.0;
        for (PurchaseOrderItem item : items) {
            totalPurchaseAmount += item.getSubtotal() != null ? item.getSubtotal() : 0.0;
        }

        long totalOrders = pos.size();
        long receivedOrders = pos.stream().filter(po -> "RECEIVED".equalsIgnoreCase(po.getStatus())).count();
        long draftOrders = pos.stream().filter(po -> "DRAFT".equalsIgnoreCase(po.getStatus())).count();

        // Group by supplier
        Map<String, SupplierPurchaseData> supplierMap = new HashMap<>();
        for (PurchaseOrder po : pos) {
            String supplierName = po.getSupplier().getName();
            supplierMap.putIfAbsent(supplierName, new SupplierPurchaseData(0L, 0.0));
            supplierMap.get(supplierName).orderCount++;
        }
        for (PurchaseOrderItem item : items) {
            String supplierName = item.getPurchaseOrder().getSupplier().getName();
            SupplierPurchaseData data = supplierMap.get(supplierName);
            if (data != null) {
                data.totalSpent += item.getSubtotal() != null ? item.getSubtotal() : 0.0;
            }
        }

        List<PurchaseReportResponse.PurchaseBySupplierSummary> purchaseBySupplier = supplierMap.entrySet().stream()
                .map(e -> new PurchaseReportResponse.PurchaseBySupplierSummary(e.getKey(), e.getValue().orderCount, e.getValue().totalSpent))
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .toList();

        // Daily breakdown
        Map<LocalDate, DailyPurchaseData> dailyMap = new HashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyMap.put(current, new DailyPurchaseData(0.0, 0L));
            current = current.plusDays(1);
        }

        for (PurchaseOrder po : pos) {
            LocalDate date = po.getOrderDate();
            DailyPurchaseData data = dailyMap.get(date);
            if (data == null) {
                data = new DailyPurchaseData(0.0, 0L);
                dailyMap.put(date, data);
            }
            data.orderCount++;
        }
        for (PurchaseOrderItem item : items) {
            LocalDate date = item.getPurchaseOrder().getOrderDate();
            DailyPurchaseData data = dailyMap.get(date);
            if (data != null) {
                data.totalSpent += item.getSubtotal() != null ? item.getSubtotal() : 0.0;
            }
        }

        List<PurchaseReportResponse.DailyPurchaseSummary> dailyPurchases = dailyMap.entrySet().stream()
                .map(e -> new PurchaseReportResponse.DailyPurchaseSummary(e.getKey(), e.getValue().totalSpent, e.getValue().orderCount))
                .sorted(Comparator.comparing(PurchaseReportResponse.DailyPurchaseSummary::getDate))
                .toList();

        return new PurchaseReportResponse(
                totalPurchaseAmount,
                totalOrders,
                receivedOrders,
                draftOrders,
                purchaseBySupplier,
                dailyPurchases
        );
    }

    @Transactional(readOnly = true)
    public InventoryReportResponse getInventoryReport() {
        List<Ingredient> ingredients = ingredientRepository.findAll();

        long totalIngredients = ingredients.size();
        long lowStockCount = 0;
        double totalInventoryValue = 0.0;
        List<InventoryReportResponse.IngredientStockStatus> statuses = new ArrayList<>();

        for (Ingredient ing : ingredients) {
            double stock = ing.getCurrentStock() != null ? ing.getCurrentStock() : 0.0;
            double min = ing.getMinimumStock() != null ? ing.getMinimumStock() : 0.0;
            double cost = ing.getCostPrice() != null ? ing.getCostPrice() : 0.0;
            double value = stock * cost;

            boolean isLow = stock < min;
            if (isLow) {
                lowStockCount++;
            }
            totalInventoryValue += value;

            statuses.add(new InventoryReportResponse.IngredientStockStatus(
                    ing.getId(),
                    ing.getCode(),
                    ing.getName(),
                    stock,
                    min,
                    cost,
                    value,
                    isLow
            ));
        }

        return new InventoryReportResponse(
                totalIngredients,
                lowStockCount,
                totalInventoryValue,
                statuses
        );
    }

    // Helper classes for grouping operations
    private static class DailySalesData {
        double totalSales;
        long count;
        DailySalesData(double totalSales, long count) {
            this.totalSales = totalSales;
            this.count = count;
        }
    }

    private static class ProductSalesData {
        long quantity;
        double revenue;
        ProductSalesData(long quantity, double revenue) {
            this.quantity = quantity;
            this.revenue = revenue;
        }
    }

    private static class SupplierPurchaseData {
        long orderCount;
        double totalSpent;
        SupplierPurchaseData(long orderCount, double totalSpent) {
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
        }
    }

    private static class DailyPurchaseData {
        double totalSpent;
        long orderCount;
        DailyPurchaseData(double totalSpent, long orderCount) {
            this.totalSpent = totalSpent;
            this.orderCount = orderCount;
        }
    }
}
