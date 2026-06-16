package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.report.*;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
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

    private LocalDate getGroupDate(LocalDate date, String groupBy) {
        if ("WEEK".equalsIgnoreCase(groupBy)) {
            return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        } else if ("MONTH".equalsIgnoreCase(groupBy)) {
            return date.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
        }
        return date;
    }

    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        return getSalesReport(startDate, endDate, "DAY");
    }

    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59, 999999999);

        // Filter out cancelled transactions
        List<Transaction> transactions = transactionRepository.findByDateRange(start, end).stream()
                .filter(t -> t.getPaymentStatus() != PaymentStatus.CANCELLED)
                .toList();
        List<TransactionItem> items = transactionItemRepository.findByTransactionDateRange(start, end).stream()
                .filter(ti -> ti.getTransaction().getPaymentStatus() != PaymentStatus.CANCELLED)
                .toList();

        double totalSalesAmount = 0.0;
        double taxAmount = 0.0;
        double totalCogs = 0.0;

        for (Transaction t : transactions) {
            totalSalesAmount += t.getTotal() != null ? t.getTotal() : 0.0;
            taxAmount += t.getTax() != null ? t.getTax() : 0.0;
        }

        for (TransactionItem item : items) {
            totalCogs += item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0;
        }

        double netSales = totalSalesAmount - taxAmount;
        double grossProfit = netSales - totalCogs;
        double grossProfitMargin = netSales > 0 ? (grossProfit / netSales) * 100 : 0.0;

        long totalTransactions = transactions.size();
        double averageTransactionValue = totalTransactions > 0 ? totalSalesAmount / totalTransactions : 0.0;

        // Daily breakdown
        Map<LocalDate, DailySalesData> dailyMap = new HashMap<>();
        if (groupBy == null || "DAY".equalsIgnoreCase(groupBy)) {
            // Pre-fill daily slots
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dailyMap.put(current, new DailySalesData(0.0, 0L, 0.0, 0.0));
                current = current.plusDays(1);
            }
        }

        for (Transaction t : transactions) {
            LocalDate date = getGroupDate(t.getCreatedAt().toLocalDate(), groupBy);
            DailySalesData data = dailyMap.get(date);
            if (data == null) {
                data = new DailySalesData(0.0, 0L, 0.0, 0.0);
                dailyMap.put(date, data);
            }
            data.totalSales += t.getTotal() != null ? t.getTotal() : 0.0;
            data.count++;
            double discount = t.getDiscountAmount() != null ? t.getDiscountAmount() : 0.0;
            double sub = t.getSubtotal() != null ? t.getSubtotal() : 0.0;
            data.netSales += (sub - discount);
        }

        for (TransactionItem item : items) {
            LocalDate date = getGroupDate(item.getTransaction().getCreatedAt().toLocalDate(), groupBy);
            DailySalesData data = dailyMap.get(date);
            if (data != null) {
                data.totalCogs += item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0;
            }
        }

        List<SalesReportResponse.DailySalesSummary> dailySales = dailyMap.entrySet().stream()
                .map(e -> {
                    double sales = e.getValue().totalSales;
                    double cogs = e.getValue().totalCogs;
                    double profit = e.getValue().netSales - cogs;
                    return new SalesReportResponse.DailySalesSummary(
                             e.getKey(), 
                             sales, 
                             e.getValue().count, 
                             cogs, 
                             profit
                    );
                })
                .sorted(Comparator.comparing(SalesReportResponse.DailySalesSummary::getDate))
                .toList();

        // Product breakdown
        Map<String, ProductSalesData> productMap = new HashMap<>();
        for (TransactionItem item : items) {
            String name = item.getProductName();
            ProductSalesData data = productMap.get(name);
            if (data == null) {
                data = new ProductSalesData(0L, 0.0, 0.0);
                productMap.put(name, data);
            }
            data.quantity += item.getQuantity() != null ? item.getQuantity() : 0;
            data.revenue += item.getSubtotal() != null ? item.getSubtotal() : 0.0;
            data.totalCogs += item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0;
        }

        List<SalesReportResponse.SalesByProductSummary> salesByProduct = productMap.entrySet().stream()
                .map(e -> {
                    double revenue = e.getValue().revenue;
                    double cogs = e.getValue().totalCogs;
                    double profit = revenue - cogs;
                    return new SalesReportResponse.SalesByProductSummary(
                            e.getKey(), 
                            e.getValue().quantity, 
                            revenue, 
                            cogs, 
                            profit
                    );
                })
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .toList();

        return new SalesReportResponse(
                totalSalesAmount,
                totalTransactions,
                averageTransactionValue,
                taxAmount,
                totalCogs,
                grossProfit,
                grossProfitMargin,
                dailySales,
                salesByProduct
        );
    }

    @Transactional(readOnly = true)
    public PurchaseReportResponse getPurchaseReport(LocalDate startDate, LocalDate endDate) {
        return getPurchaseReport(startDate, endDate, "DAY");
    }

    @Transactional(readOnly = true)
    public PurchaseReportResponse getPurchaseReport(LocalDate startDate, LocalDate endDate, String groupBy) {
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
        if (groupBy == null || "DAY".equalsIgnoreCase(groupBy)) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dailyMap.put(current, new DailyPurchaseData(0.0, 0L));
                current = current.plusDays(1);
            }
        }

        for (PurchaseOrder po : pos) {
            LocalDate date = getGroupDate(po.getOrderDate(), groupBy);
            DailyPurchaseData data = dailyMap.get(date);
            if (data == null) {
                data = new DailyPurchaseData(0.0, 0L);
                dailyMap.put(date, data);
            }
            data.orderCount++;
        }
        for (PurchaseOrderItem item : items) {
            LocalDate date = getGroupDate(item.getPurchaseOrder().getOrderDate(), groupBy);
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

    @Transactional(readOnly = true)
    public String exportSalesReportCsv(LocalDate startDate, LocalDate endDate) {
        return exportSalesReportCsv(startDate, endDate, "DAY");
    }

    @Transactional(readOnly = true)
    public String exportSalesReportCsv(LocalDate startDate, LocalDate endDate, String groupBy) {
        SalesReportResponse report = getSalesReport(startDate, endDate, groupBy);
        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("Tanggal,Jumlah Transaksi,Total Penjualan (Gross),Total HPP (COGS),Gross Profit\n");
        for (SalesReportResponse.DailySalesSummary summary : report.getDailySales()) {
            csv.append(summary.getDate()).append(",")
                    .append(summary.getTransactionCount()).append(",")
                    .append(summary.getTotalSales()).append(",")
                    .append(summary.getTotalCogs()).append(",")
                    .append(summary.getGrossProfit()).append("\n");
        }

        csv.append("\n\nProduk Terlaris\n");
        csv.append("Nama Produk,Jumlah Terjual,Total Pendapatan,Total HPP (COGS),Gross Profit\n");
        for (SalesReportResponse.SalesByProductSummary summary : report.getSalesByProduct()) {
            csv.append("\"").append(summary.getProductName().replace("\"", "\"\"")).append("\",")
                    .append(summary.getQuantitySold()).append(",")
                    .append(summary.getTotalRevenue()).append(",")
                    .append(summary.getTotalCogs()).append(",")
                    .append(summary.getGrossProfit()).append("\n");
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportPurchaseReportCsv(LocalDate startDate, LocalDate endDate) {
        return exportPurchaseReportCsv(startDate, endDate, "DAY");
    }

    @Transactional(readOnly = true)
    public String exportPurchaseReportCsv(LocalDate startDate, LocalDate endDate, String groupBy) {
        PurchaseReportResponse report = getPurchaseReport(startDate, endDate, groupBy);
        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("Tanggal,Jumlah Order,Total Pengeluaran Pembelian\n");
        for (PurchaseReportResponse.DailyPurchaseSummary summary : report.getDailyPurchases()) {
            csv.append(summary.getDate()).append(",")
                    .append(summary.getOrderCount()).append(",")
                    .append(summary.getTotalSpent()).append("\n");
        }

        csv.append("\n\nPembelian per Supplier\n");
        csv.append("Nama Supplier,Jumlah Order,Total Pembelian\n");
        for (PurchaseReportResponse.PurchaseBySupplierSummary summary : report.getPurchaseBySupplier()) {
            csv.append("\"").append(summary.getSupplierName().replace("\"", "\"\"")).append("\",")
                    .append(summary.getOrderCount()).append(",")
                    .append(summary.getTotalSpent()).append("\n");
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportInventoryReportCsv() {
        InventoryReportResponse report = getInventoryReport();
        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("ID Bahan,Kode,Nama Bahan,Stok Saat Ini,Stok Minimum,Harga Beli (Modal),Total Nilai Inventaris,Stok Rendah (Alert)\n");
        for (InventoryReportResponse.IngredientStockStatus status : report.getIngredientStockStatus()) {
            csv.append(status.getIngredientId()).append(",")
                    .append(status.getIngredientCode()).append(",")
                    .append("\"").append(status.getIngredientName().replace("\"", "\"\"")).append("\",")
                    .append(status.getCurrentStock()).append(",")
                    .append(status.getMinimumStock()).append(",")
                    .append(status.getCostPrice()).append(",")
                    .append(status.getTotalValue()).append(",")
                    .append(status.getIsLowStock() ? "YA" : "TIDAK").append("\n");
        }
        return csv.toString();
    }

    // Helper classes for grouping operations
    private static class DailySalesData {
        double totalSales;
        long count;
        double totalCogs;
        double netSales;
        DailySalesData(double totalSales, long count, double totalCogs, double netSales) {
            this.totalSales = totalSales;
            this.count = count;
            this.totalCogs = totalCogs;
            this.netSales = netSales;
        }
    }

    private static class ProductSalesData {
        long quantity;
        double revenue;
        double totalCogs;
        ProductSalesData(long quantity, double revenue, double totalCogs) {
            this.quantity = quantity;
            this.revenue = revenue;
            this.totalCogs = totalCogs;
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
