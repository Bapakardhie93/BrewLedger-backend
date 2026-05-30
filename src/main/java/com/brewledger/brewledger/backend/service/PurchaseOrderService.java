package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderItemResponse;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.PurchaseOrder;
import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.entity.StockMovement;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderItemRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderRepository;
import com.brewledger.brewledger.backend.repository.StockMovementRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderItemRepository itemRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<PurchaseOrderItemResponse> getItems(Long purchaseOrderId) {

        return itemRepository
                .findByPurchaseOrderId(purchaseOrderId)
                .stream()
                .map(item -> new PurchaseOrderItemResponse(
                        item.getId(),
                        item.getIngredient().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();
    }

    /**
     * Receives a purchase order: updates ingredient stock and records movements.
     * The entire operation is atomic - if any stock update fails, everything rolls back.
     *
     * Bug Fix #3: Added @Transactional to ensure all stock updates are atomic.
     * Bug Fix #4: Use PurchaseOrderStatus enum consistently instead of raw Strings.
     */
    @Transactional
    public PurchaseOrderResponse receive(Long purchaseOrderId) {

        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        // Bug Fix #4: Compare using enum name() to avoid magic string comparison
        if (!PurchaseOrderStatus.DRAFT.name().equals(po.getStatus())) {
            throw new BusinessException(
                    "PO tidak dapat diterima karena statusnya bukan DRAFT. Status saat ini: " + po.getStatus()
            );
        }

        List<PurchaseOrderItem> items = itemRepository.findByPurchaseOrderId(purchaseOrderId);

        if (items.isEmpty()) {
            throw new BusinessException("Tidak dapat menerima PO yang tidak memiliki item");
        }

        for (PurchaseOrderItem item : items) {

            Ingredient ingredient = item.getIngredient();
            Double stockBefore = ingredient.getCurrentStock();
            Double stockAfter = stockBefore + item.getQuantity();

            ingredient.setCurrentStock(stockAfter);
            ingredientRepository.save(ingredient);

            StockMovement movement = new StockMovement();
            movement.setIngredient(ingredient);
            movement.setQuantity(item.getQuantity());
            movement.setStockBefore(stockBefore);
            movement.setStockAfter(stockAfter);
            movement.setMovementType("PURCHASE");
            movement.setReferenceNumber(po.getPoNumber());
            movement.setMovementDate(LocalDateTime.now());

            stockMovementRepository.save(movement);
        }

        po.setStatus(PurchaseOrderStatus.RECEIVED.name());
        purchaseOrderRepository.save(po);

        log.info("Purchase Order received: {}", po.getPoNumber());

        return mapToResponse(po);
    }

    /**
     * Adds an item to a DRAFT purchase order.
     *
     * @throws BusinessException if PO is not in DRAFT status
     */
    @Transactional
    public PurchaseOrderItemResponse addItem(
            Long purchaseOrderId,
            CreatePurchaseOrderItemRequest request
    ) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        if (!PurchaseOrderStatus.DRAFT.name().equals(po.getStatus())) {
            throw new BusinessException(
                    "Tidak dapat menambah item ke PO yang sudah diproses. Status saat ini: " + po.getStatus()
            );
        }

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + request.getIngredientId()
                ));

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(po);
        item.setIngredient(ingredient);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setSubtotal(request.getQuantity() * request.getUnitPrice());
        item.setStatus(PurchaseOrderStatus.DRAFT);

        itemRepository.save(item);

        return new PurchaseOrderItemResponse(
                item.getId(),
                ingredient.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    @Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + request.getSupplierId()
                ));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setOrderDate(LocalDate.now());
        po.setStatus(PurchaseOrderStatus.DRAFT.name());
        po.setNotes(request.getNotes());

        purchaseOrderRepository.save(po);

        return mapToResponse(po);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {

        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder po) {

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getStatus(),
                po.getNotes()
        );
    }
}