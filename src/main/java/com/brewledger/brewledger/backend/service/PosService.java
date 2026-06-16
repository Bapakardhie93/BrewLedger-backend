package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.pos.PosCatalogResponse;
import com.brewledger.brewledger.backend.dto.pos.PosProductResponse;
import com.brewledger.brewledger.backend.dto.pos.PosSummaryResponse;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductRecipe;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.entity.CashierShift;
import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.ShiftStatus;
import com.brewledger.brewledger.backend.enums.KitchenOrderStatus;
import com.brewledger.brewledger.backend.repository.ProductRecipeRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import com.brewledger.brewledger.backend.repository.CashierShiftRepository;
import com.brewledger.brewledger.backend.repository.TransactionRepository;
import com.brewledger.brewledger.backend.repository.KitchenOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PosService {

    public static final double TAX_RATE = 0.11;

    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final CashierShiftRepository cashierShiftRepository;
    private final TransactionRepository transactionRepository;
    private final KitchenOrderRepository kitchenOrderRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public PosCatalogResponse getCatalog(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        List<Product> products = normalizedKeyword.isEmpty()
                ? productRepository.findByActiveTrueOrderByNameAsc()
                : productRepository.findActiveByNameOrCode(normalizedKeyword);

        List<PosProductResponse> productResponses = products.stream()
                .map(this::mapProduct)
                .toList();

        return new PosCatalogResponse(
                currentUserService.requireCurrentUser().getFullName(),
                TAX_RATE,
                Arrays.stream(PaymentMethod.values()).map(Enum::name).toList(),
                productResponses
        );
    }

    private PosProductResponse mapProduct(Product product) {
        List<ProductRecipe> recipes = productRecipeRepository.findByProductId(product.getId());
        Long maximumOrderQuantity = calculateMaximumOrderQuantity(recipes);

        return new PosProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getCategory() != null ? product.getCategory().getName() : "Tanpa Kategori",
                product.getSellingPrice(),
                maximumOrderQuantity == null || maximumOrderQuantity > 0,
                maximumOrderQuantity
        );
    }

    private Long calculateMaximumOrderQuantity(List<ProductRecipe> recipes) {
        if (recipes.isEmpty()) {
            return null;
        }

        long minQty = recipes.stream()
                .mapToLong(recipe -> {
                    double required = recipe.getQuantityRequired();
                    double stock = recipe.getIngredient().getCurrentStock();
                    return required > 0 ? (long) Math.floor(stock / required) : 0;
                })
                .min()
                .orElse(0);

        return Math.max(0L, minQty);
    }

    @Transactional(readOnly = true)
    public PosSummaryResponse getSummary() {
        User currentUser = currentUserService.requireCurrentUser();
        java.util.Optional<CashierShift> activeShiftOpt = cashierShiftRepository
                .findByCashierIdAndStatus(currentUser.getId(), ShiftStatus.OPEN);

        boolean shiftActive = activeShiftOpt.isPresent();
        Long activeShiftId = shiftActive ? activeShiftOpt.get().getId() : null;

        java.time.LocalDateTime todayStart = java.time.LocalDate.now().atStartOfDay();
        java.util.List<com.brewledger.brewledger.backend.entity.Transaction> todayTx = transactionRepository
                .findTodayCashierTransactions(currentUser.getId(), todayStart);

        long todaySalesCount = todayTx.size();
        double todaySalesAmount = todayTx.stream()
                .filter(t -> t.getPaymentStatus() == com.brewledger.brewledger.backend.enums.PaymentStatus.PAID)
                .mapToDouble(t -> t.getTotal() != null ? t.getTotal() : 0.0)
                .sum();

        long pendingOrdersCount = kitchenOrderRepository.countByTransactionCashierIdAndStatusIn(
                currentUser.getId(),
                List.of(KitchenOrderStatus.WAITING, KitchenOrderStatus.PREPARING, KitchenOrderStatus.READY)
        );

        return new PosSummaryResponse(
                shiftActive,
                activeShiftId,
                todaySalesCount,
                todaySalesAmount,
                pendingOrdersCount
        );
    }
}
