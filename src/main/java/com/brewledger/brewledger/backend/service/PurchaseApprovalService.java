package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.warehouse.PurchaseApprovalResponse;
import com.brewledger.brewledger.backend.entity.PurchaseOrder;
import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.PurchaseOrderItemRepository;
import com.brewledger.brewledger.backend.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseApprovalService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<PurchaseApprovalResponse> findPendingApprovals() {
        return purchaseOrderRepository.findByStatusInOrderByCreatedAtDesc(
                        List.of(PurchaseOrderStatus.PENDING_APPROVAL.name())
                )
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional
    public PurchaseApprovalResponse approve(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = requirePendingOrder(purchaseOrderId);
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED.name());
        purchaseOrder.setApprovedBy(currentUserService.requireCurrentUser());
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setRejectionReason(null);

        return mapResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseApprovalResponse reject(Long purchaseOrderId, String reason) {
        PurchaseOrder purchaseOrder = requirePendingOrder(purchaseOrderId);
        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED.name());
        purchaseOrder.setApprovedBy(currentUserService.requireCurrentUser());
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setRejectionReason(reason.trim());

        return mapResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    private PurchaseOrder requirePendingOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        if (!PurchaseOrderStatus.PENDING_APPROVAL.name().equals(purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "PO tidak sedang menunggu persetujuan. Status saat ini: "
                            + purchaseOrder.getStatus()
            );
        }

        return purchaseOrder;
    }

    private PurchaseApprovalResponse mapResponse(PurchaseOrder purchaseOrder) {
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
                items.stream()
                        .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal() : 0.0)
                        .sum(),
                purchaseOrder.getNotes(),
                purchaseOrder.getRejectionReason()
        );
    }
}
