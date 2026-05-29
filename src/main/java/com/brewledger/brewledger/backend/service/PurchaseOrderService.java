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

import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderItemRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;

import com.brewledger.brewledger.backend.entity.StockMovement;
import com.brewledger.brewledger.backend.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderItemRepository itemRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;

    public List<PurchaseOrderItemResponse> getItems(
            Long purchaseOrderId
    ) {

        return itemRepository
                .findByPurchaseOrderId(
                        purchaseOrderId
                )
                .stream()
                .map(item ->
                        new PurchaseOrderItemResponse(
                                item.getId(),
                                item.getIngredient().getName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        )
                )
                .toList();
    }

    public PurchaseOrderResponse receive(
            Long purchaseOrderId
    ) {

        PurchaseOrder po =
                purchaseOrderRepository.findById(
                                purchaseOrderId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Purchase Order tidak ditemukan"
                                ));

        if (!po.getStatus().equals("DRAFT")) {
            throw new RuntimeException(
                    "PO sudah diproses"
            );
        }

        List<PurchaseOrderItem> items =
                itemRepository.findByPurchaseOrderId(
                        purchaseOrderId
                );

        if (items.isEmpty()) {
            throw new RuntimeException(
                    "PO belum memiliki item"
            );
        }

        for (PurchaseOrderItem item : items) {

            Ingredient ingredient =
                    item.getIngredient();

            Double stockBefore =
                    ingredient.getCurrentStock();

            Double stockAfter =
                    stockBefore + item.getQuantity();

            ingredient.setCurrentStock(
                    stockAfter
            );

            ingredientRepository.save(
                    ingredient
            );

            StockMovement movement =
                    new StockMovement();

            movement.setIngredient(
                    ingredient
            );

            movement.setQuantity(
                    item.getQuantity()
            );

            movement.setStockBefore(
                    stockBefore
            );

            movement.setStockAfter(
                    stockAfter
            );

            movement.setMovementType(
                    "PURCHASE"
            );

            movement.setReferenceNumber(
                    po.getPoNumber()
            );

            movement.setMovementDate(
                    java.time.LocalDateTime.now()
            );

            stockMovementRepository.save(
                    movement
            );
        }

        po.setStatus("RECEIVED");

        purchaseOrderRepository.save(
                po
        );

        return mapToResponse(po);
    }

    public PurchaseOrderItemResponse addItem(
            Long purchaseOrderId,
            CreatePurchaseOrderItemRequest request
    ) {

        PurchaseOrder po =
                purchaseOrderRepository.findById(
                                purchaseOrderId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Purchase Order tidak ditemukan"
                                ));

        if (!"DRAFT".equals(po.getStatus())) {
            throw new RuntimeException(
                    "Tidak dapat menambah item ke PO yang sudah diproses"
            );
        }

        Ingredient ingredient =
                ingredientRepository.findById(
                                request.getIngredientId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Ingredient tidak ditemukan"
                                ));

        PurchaseOrderItem item =
                new PurchaseOrderItem();

        item.setPurchaseOrder(po);

        item.setIngredient(
                ingredient
        );

        item.setQuantity(
                request.getQuantity()
        );

        item.setUnitPrice(
                request.getUnitPrice()
        );

        item.setSubtotal(
                request.getQuantity()
                        * request.getUnitPrice()
        );

        item.setStatus(
                PurchaseOrderStatus.DRAFT
        );

        itemRepository.save(item);

        return new PurchaseOrderItemResponse(
                item.getId(),
                ingredient.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    public PurchaseOrderResponse create(
            CreatePurchaseOrderRequest request
    ) {

        Supplier supplier =
                supplierRepository.findById(
                        request.getSupplierId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Supplier tidak ditemukan"
                        ));

        PurchaseOrder po =
                new PurchaseOrder();

        po.setPoNumber(
                "PO-" + System.currentTimeMillis()
        );

        po.setSupplier(supplier);

        po.setOrderDate(
                LocalDate.now()
        );

        po.setStatus("DRAFT");

        po.setNotes(
                request.getNotes()
        );

        purchaseOrderRepository.save(po);

        return mapToResponse(po);
    }

    public List<PurchaseOrderResponse> findAll() {

        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PurchaseOrderResponse mapToResponse(
            PurchaseOrder po
    ) {

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