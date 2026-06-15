package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.dashboard.CategorySalesResponse;
import com.brewledger.brewledger.backend.dto.dashboard.CriticalStockResponse;
import com.brewledger.brewledger.backend.dto.dashboard.DailyDashboardMetricResponse;
import com.brewledger.brewledger.backend.dto.dashboard.DashboardResponse;
import com.brewledger.brewledger.backend.dto.dashboard.RecentStockMovementResponse;
import com.brewledger.brewledger.backend.dto.dashboard.RecentTransactionResponse;
import com.brewledger.brewledger.backend.dto.dashboard.TopSellingProductResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.entity.Transaction;
import com.brewledger.brewledger.backend.entity.TransactionItem;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderRepository;
import com.brewledger.brewledger.backend.repository.StockMovementRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import com.brewledger.brewledger.backend.repository.TransactionItemRepository;
import com.brewledger.brewledger.backend.repository.TransactionRepository;
import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DEFAULT_LIST_LIMIT = 5;
    private static final int RECENT_TRANSACTION_LIMIT = 8;

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final TransactionRepository transactionRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate sevenDaysAgo = today.minusDays(6);

        List<Transaction> todayTransactionData = findTransactions(today, today);
        List<Transaction> yesterdayTransactionData = findTransactions(yesterday, yesterday);
        List<Transaction> lastSevenDayTransactions = findTransactions(sevenDaysAgo, today);
        List<TransactionItem> lastSevenDayItems = transactionItemRepository.findByTransactionDateRange(
                sevenDaysAgo.atStartOfDay(),
                today.atTime(23, 59, 59, 999999999)
        );
        List<Ingredient> criticalIngredients = ingredientRepository.findLowStock();

        double todaySales = sumPaidSales(todayTransactionData);
        double yesterdaySales = sumPaidSales(yesterdayTransactionData);
        long todayTransactions = countPaidTransactions(todayTransactionData);
        long yesterdayTransactions = countPaidTransactions(yesterdayTransactionData);

        DashboardResponse response = new DashboardResponse();
        response.setGeneratedAt(LocalDateTime.now());
        response.setTodaySales(todaySales);
        response.setSalesChangePercentage(calculatePercentageChange(todaySales, yesterdaySales));
        response.setTodayTransactions(todayTransactions);
        response.setTransactionChangePercentage(
                calculatePercentageChange(todayTransactions, yesterdayTransactions)
        );
        response.setCriticalStockCount((long) criticalIngredients.size());
        response.setPendingApprovalCount(
                purchaseOrderRepository.countByStatusIgnoreCase(
                        PurchaseOrderStatus.PENDING_APPROVAL.name()
                )
        );
        response.setActiveUsers(userRepository.countByActiveTrue());

        response.setTotalProducts(productRepository.count());
        response.setTotalIngredients(ingredientRepository.count());
        response.setTotalSuppliers(supplierRepository.count());
        response.setTotalTransactions(transactionRepository.count());
        response.setTotalSales(transactionRepository.getTotalSales());
        response.setTotalStockMovements(stockMovementRepository.count());

        response.setTopSellingProducts(
                transactionItemRepository.findTopSellingProducts(PageRequest.of(0, DEFAULT_LIST_LIMIT))
        );
        response.setRecentStockMovements(getRecentStockMovements());
        response.setRecentTransactions(getRecentTransactions());
        response.setTopMovingIngredients(
                stockMovementRepository.findTopMovingIngredients(PageRequest.of(0, DEFAULT_LIST_LIMIT))
        );
        response.setLastSevenDays(buildDailyMetrics(sevenDaysAgo, today, lastSevenDayTransactions));
        response.setSalesByCategory(buildCategorySales(lastSevenDayItems));
        response.setCriticalStocks(buildCriticalStocks(criticalIngredients));

        return response;
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        if (limit < 1 || limit > 100) {
            throw new BusinessException("Limit produk terlaris harus antara 1 dan 100");
        }

        return transactionItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }

    private List<Transaction> findTransactions(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59, 999999999)
        );
    }

    private List<RecentStockMovementResponse> getRecentStockMovements() {
        return stockMovementRepository.findRecentMovements(PageRequest.of(0, DEFAULT_LIST_LIMIT))
                .stream()
                .map(movement -> new RecentStockMovementResponse(
                        movement.getIngredient().getName(),
                        movement.getIngredient().getName(),
                        isStockIn(movement.getMovementType()) ? "IN" : "OUT",
                        movement.getQuantity()
                ))
                .toList();
    }

    private List<RecentTransactionResponse> getRecentTransactions() {
        return transactionRepository.findRecentTransactions(PageRequest.of(0, RECENT_TRANSACTION_LIMIT))
                .stream()
                .map(transaction -> new RecentTransactionResponse(
                        transaction.getId(),
                        transaction.getTransactionNumber(),
                        hasText(transaction.getNotes()) ? transaction.getNotes().trim() : "Umum",
                        transaction.getCashier() != null
                                ? transaction.getCashier().getFullName()
                                : "Tidak diketahui",
                        transaction.getCreatedAt(),
                        valueOrZero(transaction.getTotal()),
                        transaction.getPaymentStatus() != null
                                ? transaction.getPaymentStatus().name()
                                : "UNKNOWN",
                        transaction.getTransactionType() != null
                                ? transaction.getTransactionType().name()
                                : null,
                        transaction.getPaymentMethod() != null
                                ? transaction.getPaymentMethod().name()
                                : null
                ))
                .toList();
    }

    private List<DailyDashboardMetricResponse> buildDailyMetrics(
            LocalDate startDate,
            LocalDate endDate,
            List<Transaction> transactions
    ) {
        Map<LocalDate, DailyAccumulator> metrics = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            metrics.put(date, new DailyAccumulator());
        }

        transactions.stream()
                .filter(this::isPaid)
                .forEach(transaction -> {
                    LocalDate date = transaction.getCreatedAt().toLocalDate();
                    DailyAccumulator accumulator = metrics.computeIfAbsent(
                            date,
                            ignored -> new DailyAccumulator()
                    );
                    accumulator.sales += valueOrZero(transaction.getTotal());
                    accumulator.transactionCount++;
                });

        return metrics.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DailyDashboardMetricResponse(
                        entry.getKey(),
                        getIndonesianDayLabel(entry.getKey().getDayOfWeek()),
                        round(entry.getValue().sales),
                        entry.getValue().transactionCount
                ))
                .toList();
    }

    private List<CategorySalesResponse> buildCategorySales(List<TransactionItem> items) {
        Map<String, CategoryAccumulator> categoryData = new HashMap<>();

        items.stream()
                .filter(item -> item.getTransaction() != null && isPaid(item.getTransaction()))
                .forEach(item -> {
                    String categoryName = getCategoryName(item.getProduct());
                    CategoryAccumulator accumulator = categoryData.computeIfAbsent(
                            categoryName,
                            ignored -> new CategoryAccumulator()
                    );
                    accumulator.quantitySold += item.getQuantity() != null ? item.getQuantity() : 0;
                    accumulator.revenue += valueOrZero(item.getSubtotal());
                });

        double totalRevenue = categoryData.values().stream()
                .mapToDouble(accumulator -> accumulator.revenue)
                .sum();

        return categoryData.entrySet().stream()
                .map(entry -> new CategorySalesResponse(
                        entry.getKey(),
                        entry.getValue().quantitySold,
                        round(entry.getValue().revenue),
                        totalRevenue > 0
                                ? round((entry.getValue().revenue / totalRevenue) * 100)
                                : 0.0
                ))
                .sorted(Comparator.comparing(CategorySalesResponse::getRevenue).reversed())
                .toList();
    }

    private List<CriticalStockResponse> buildCriticalStocks(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> new CriticalStockResponse(
                        ingredient.getId(),
                        ingredient.getCode(),
                        ingredient.getName(),
                        ingredient.getUnit(),
                        valueOrZero(ingredient.getCurrentStock()),
                        valueOrZero(ingredient.getMinimumStock()),
                        Math.max(
                                0.0,
                                round(valueOrZero(ingredient.getMinimumStock())
                                        - valueOrZero(ingredient.getCurrentStock()))
                        )
                ))
                .sorted(Comparator.comparing(CriticalStockResponse::getShortage).reversed())
                .toList();
    }

    private double sumPaidSales(List<Transaction> transactions) {
        return round(transactions.stream()
                .filter(this::isPaid)
                .mapToDouble(transaction -> valueOrZero(transaction.getTotal()))
                .sum());
    }

    private long countPaidTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .filter(this::isPaid)
                .count();
    }

    private boolean isPaid(Transaction transaction) {
        return transaction.getPaymentStatus() == PaymentStatus.PAID;
    }

    private boolean isStockIn(String movementType) {
        return "PURCHASE".equalsIgnoreCase(movementType)
                || "IN".equalsIgnoreCase(movementType);
    }

    private String getCategoryName(Product product) {
        if (product == null) {
            return "Tanpa Kategori";
        }

        ProductCategory category = product.getCategory();
        return category != null ? category.getName() : "Tanpa Kategori";
    }

    private double calculatePercentageChange(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return currentValue == 0 ? 0.0 : 100.0;
        }

        return round(((currentValue - previousValue) / previousValue) * 100);
    }

    private String getIndonesianDayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Sen";
            case TUESDAY -> "Sel";
            case WEDNESDAY -> "Rab";
            case THURSDAY -> "Kam";
            case FRIDAY -> "Jum";
            case SATURDAY -> "Sab";
            case SUNDAY -> "Min";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private double valueOrZero(Double value) {
        return value != null ? value : 0.0;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static class DailyAccumulator {
        private double sales;
        private long transactionCount;
    }

    private static class CategoryAccumulator {
        private long quantitySold;
        private double revenue;
    }
}
