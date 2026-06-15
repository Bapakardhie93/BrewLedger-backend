package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.stockmovement.StockMovementResponse;
import com.brewledger.brewledger.backend.dto.warehouse.PurchaseApprovalResponse;
import com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest;
import com.brewledger.brewledger.backend.dto.warehouse.UpdateWarehouseIngredientRequest;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseIngredientResponse;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseRecipeResponse;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.ProductRecipe;
import com.brewledger.brewledger.backend.entity.PurchaseOrder;
import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.entity.StockMovement;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.ProductRecipeRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderItemRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderRepository;
import com.brewledger.brewledger.backend.repository.StockMovementRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public WarehouseResponse getWorkspace(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        List<Ingredient> allIngredients = ingredientRepository.findAll();
        List<Ingredient> visibleIngredients = normalizedKeyword.isEmpty()
                ? allIngredients
                : ingredientRepository.findByNameContainingIgnoreCase(normalizedKeyword);

        List<PurchaseOrder> approvalRequests = purchaseOrderRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(
                        PurchaseOrderStatus.DRAFT.name(),
                        PurchaseOrderStatus.PENDING_APPROVAL.name(),
                        PurchaseOrderStatus.APPROVED.name(),
                        PurchaseOrderStatus.REJECTED.name()
                )
        );

        double totalStock = allIngredients.stream()
                .mapToDouble(ingredient -> valueOrZero(ingredient.getCurrentStock()))
                .sum();

        long lowStockCount = allIngredients.stream()
                .filter(ingredient -> valueOrZero(ingredient.getCurrentStock())
                        <= valueOrZero(ingredient.getMinimumStock()))
                .count();

        return new WarehouseResponse(
                LocalDateTime.now(),
                (long) allIngredients.size(),
                totalStock,
                lowStockCount,
                purchaseOrderRepository.countByStatusIgnoreCase(
                        PurchaseOrderStatus.PENDING_APPROVAL.name()
                ),
                visibleIngredients.stream().map(this::mapIngredient).toList(),
                productRecipeRepository.findAll().stream()
                        .map(this::mapRecipe)
                        .toList(),
                stockMovementRepository.findAll().stream()
                        .sorted(Comparator.comparing(
                                StockMovement::getMovementDate,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                        .map(this::mapMovement)
                        .toList(),
                approvalRequests.stream().map(this::mapApproval).toList()
        );
    }

    @Transactional
    public WarehouseIngredientResponse updateIngredient(
            Long ingredientId,
            UpdateWarehouseIngredientRequest request
    ) {
        Ingredient ingredient = requireIngredient(ingredientId);
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + request.getSupplierId()
                ));

        ingredient.setName(request.getName());
        ingredient.setSupplier(supplier);
        ingredient.setUnit(request.getUnit());
        ingredient.setMinimumStock(request.getMinimumStock());
        ingredient.setCostPrice(request.getCostPrice());
        ingredient.setActive(request.getActive());

        return mapIngredient(ingredientRepository.save(ingredient));
    }

    @Transactional
    public WarehouseIngredientResponse adjustStock(
            Long ingredientId,
            StockAdjustmentRequest request
    ) {
        Ingredient ingredient = requireIngredient(ingredientId);
        double stockBefore = valueOrZero(ingredient.getCurrentStock());
        double stockAfter = request.getNewStock();

        ingredient.setCurrentStock(stockAfter);
        ingredientRepository.save(ingredient);

        StockMovement movement = new StockMovement();
        movement.setIngredient(ingredient);
        movement.setQuantity(Math.abs(stockAfter - stockBefore));
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setMovementType(stockAfter >= stockBefore ? "ADJUSTMENT_IN" : "ADJUSTMENT_OUT");
        String reason = request.getReason().trim();
        String shortenedReason = reason.length() > 80 ? reason.substring(0, 80) : reason;
        movement.setReferenceNumber(
                "ADJ-" + currentUserService.requireCurrentUser().getUsername()
                        + "-" + System.currentTimeMillis() + "-" + shortenedReason
        );
        movement.setMovementDate(LocalDateTime.now());
        stockMovementRepository.save(movement);

        return mapIngredient(ingredient);
    }

    private Ingredient requireIngredient(Long ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + ingredientId
                ));
    }

    private WarehouseIngredientResponse mapIngredient(Ingredient ingredient) {
        boolean lowStock = valueOrZero(ingredient.getCurrentStock())
                <= valueOrZero(ingredient.getMinimumStock());

        return new WarehouseIngredientResponse(
                ingredient.getId(),
                ingredient.getCode(),
                ingredient.getName(),
                ingredient.getUnit(),
                valueOrZero(ingredient.getCurrentStock()),
                valueOrZero(ingredient.getMinimumStock()),
                valueOrZero(ingredient.getCostPrice()),
                ingredient.getSupplier() != null ? ingredient.getSupplier().getName() : "Tanpa Supplier",
                lowStock ? "LOW_STOCK" : "SAFE",
                ingredient.getActive()
        );
    }

    private WarehouseRecipeResponse mapRecipe(ProductRecipe recipe) {
        return new WarehouseRecipeResponse(
                recipe.getId(),
                recipe.getProduct().getId(),
                recipe.getProduct().getCode(),
                recipe.getProduct().getName(),
                recipe.getIngredient().getId(),
                recipe.getIngredient().getName(),
                recipe.getIngredient().getUnit(),
                recipe.getQuantityRequired()
        );
    }

    private StockMovementResponse mapMovement(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getIngredient().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getStockBefore(),
                movement.getStockAfter(),
                movement.getReferenceNumber(),
                movement.getMovementDate()
        );
    }

    private PurchaseApprovalResponse mapApproval(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());

        return new PurchaseApprovalResponse(
                purchaseOrder.getId(),
                purchaseOrder.getPoNumber(),
                purchaseOrder.getSupplier().getName(),
                purchaseOrder.getOrderDate(),
                purchaseOrder.getStatus(),
                purchaseOrder.getCreatedBy() != null
                        ? purchaseOrder.getCreatedBy().getFullName()
                        : "Tidak diketahui",
                purchaseOrder.getSubmittedAt(),
                (long) items.size(),
                items.stream().mapToDouble(item -> valueOrZero(item.getSubtotal())).sum(),
                purchaseOrder.getNotes(),
                purchaseOrder.getRejectionReason()
        );
    }

    private double valueOrZero(Double value) {
        return value != null ? value : 0.0;
    }
}
