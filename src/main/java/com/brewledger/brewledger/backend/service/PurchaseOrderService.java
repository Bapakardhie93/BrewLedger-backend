package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderItemResponse;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderDetailResponse;
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
@SuppressWarnings("null")
public class PurchaseOrderService {

    private final PurchaseOrderItemRepository itemRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderItemResponse> getItems(@org.springframework.lang.NonNull Long purchaseOrderId) {

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
    public PurchaseOrderResponse receive(@org.springframework.lang.NonNull Long purchaseOrderId) {

        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        // Bug Fix #4: Compare using enum name() to avoid magic string comparison
        if (!PurchaseOrderStatus.APPROVED.name().equals(po.getStatus())) {
            throw new BusinessException(
                    "PO tidak dapat diterima karena belum disetujui. Status saat ini: " + po.getStatus()
            );
        }

        List<PurchaseOrderItem> items = itemRepository.findByPurchaseOrderId(purchaseOrderId);

        if (items.isEmpty()) {
            throw new BusinessException("Tidak dapat menerima PO yang tidak memiliki item");
        }

        String username = currentUserService.requireCurrentUser().getUsername();

        for (PurchaseOrderItem item : items) {

            Ingredient ingredient = item.getIngredient();
            Double stockBefore = ingredient.getCurrentStock();
            Double stockAfter = stockBefore + item.getQuantity();

            ingredient.setCurrentStock(stockAfter);
            ingredient.setPurchasePrice(item.getUnitPrice());
            if (ingredient.getPackSize() != null && ingredient.getPackSize() > 0.0) {
                ingredient.setCostPrice(item.getUnitPrice() / ingredient.getPackSize());
            } else {
                ingredient.setCostPrice(item.getUnitPrice());
            }
            ingredientRepository.save(ingredient);

            StockMovement movement = new StockMovement();
            movement.setIngredient(ingredient);
            movement.setQuantity(item.getQuantity());
            movement.setStockBefore(stockBefore);
            movement.setStockAfter(stockAfter);
            movement.setMovementType("PURCHASE_RECEIVE");
            movement.setReferenceNumber(po.getPoNumber());
            movement.setMovementDate(LocalDateTime.now());
            movement.setCreatedBy(username);

            stockMovementRepository.save(movement);
        }

        po.setStatus(PurchaseOrderStatus.RECEIVED.name());
        purchaseOrderRepository.save(po);

        // Record Audit Log and Send Notification
        activityLogService.record("RECEIVE_PURCHASE_ORDER", 
                "Received Purchase Order: " + po.getPoNumber() + " by " + username,
                "PURCHASE_ORDER", po.getId());
        notificationService.sendAlert("Purchase Order Diterima: " + po.getPoNumber() + " telah diterima oleh " + username);

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
            @org.springframework.lang.NonNull Long purchaseOrderId,
            CreatePurchaseOrderItemRequest request
    ) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        boolean editable = PurchaseOrderStatus.DRAFT.name().equals(po.getStatus())
                || PurchaseOrderStatus.REJECTED.name().equals(po.getStatus());

        if (!editable) {
            throw new BusinessException(
                    "Tidak dapat menambah item ke PO yang sudah diproses. Status saat ini: " + po.getStatus()
            );
        }

        Long ingredientId = java.util.Objects.requireNonNull(request.getIngredientId(), "Ingredient ID must not be null");
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
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

        Long supplierId = java.util.Objects.requireNonNull(request.getSupplierId(), "Supplier ID must not be null");
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + request.getSupplierId()
                ));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + System.currentTimeMillis());
        po.setSupplier(supplier);
        po.setOrderDate(LocalDate.now());
        po.setStatus(PurchaseOrderStatus.DRAFT.name());
        po.setNotes(request.getNotes());
        po.setCreatedBy(currentUserService.requireCurrentUser());

        purchaseOrderRepository.save(po);

        // Record Audit Log
        activityLogService.record("CREATE_PURCHASE_ORDER", 
                "Created Purchase Order: " + po.getPoNumber() + " for supplier " + supplier.getName(),
                "PURCHASE_ORDER", po.getId());

        return mapToResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse submitForApproval(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        boolean submittable = PurchaseOrderStatus.DRAFT.name().equals(po.getStatus())
                || PurchaseOrderStatus.REJECTED.name().equals(po.getStatus());
        if (!submittable) {
            throw new BusinessException(
                    "PO tidak dapat diajukan dari status: " + po.getStatus()
            );
        }

        if (itemRepository.findByPurchaseOrderId(purchaseOrderId).isEmpty()) {
            throw new BusinessException("PO harus memiliki minimal 1 item sebelum diajukan");
        }

        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL.name());
        po.setSubmittedAt(LocalDateTime.now());
        po.setApprovedBy(null);
        po.setApprovedAt(null);
        po.setRejectionReason(null);

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // Record Audit Log & Send Notification
        String username = currentUserService.requireCurrentUser().getUsername();
        activityLogService.record("SUBMIT_PURCHASE_ORDER", 
                "Submitted Purchase Order for approval: " + savedPo.getPoNumber() + " by " + username,
                "PURCHASE_ORDER", savedPo.getId());
        notificationService.sendAlert("Purchase Order Baru Diajukan: " + savedPo.getPoNumber() 
                + " untuk supplier " + savedPo.getSupplier().getName() + " oleh " + username + ". Membutuhkan Approval.");

        return mapToResponse(savedPo);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {

        return purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse findById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + id
                ));

        List<PurchaseOrderItemResponse> items = getItems(id);

        return new PurchaseOrderDetailResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getStatus(),
                po.getNotes(),
                items
        );
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
