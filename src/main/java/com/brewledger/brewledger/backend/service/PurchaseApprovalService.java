package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.warehouse.PurchaseApprovalResponse;
import com.brewledger.brewledger.backend.entity.PurchaseOrder;
import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.entity.User;
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
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

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
        User approver = currentUserService.requireCurrentUser();

        // 1. User yang membuat pengajuan tidak boleh approve/reject pengajuannya sendiri
        if (purchaseOrder.getCreatedBy() != null && purchaseOrder.getCreatedBy().getId().equals(approver.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        // 2. Hanya MANAGEMENT yang boleh menyetujui
        if (!approver.getRole().getName().equalsIgnoreCase("MANAGEMENT")) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED.name());
        purchaseOrder.setApprovedBy(approver);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setRejectionReason(null);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        // Record Audit Log and Send Notification
        String username = approver.getUsername();
        activityLogService.record("APPROVE_PURCHASE_ORDER", 
                "Approved Purchase Order: " + saved.getPoNumber() + " by " + username,
                "PURCHASE_ORDER", saved.getId());
        notificationService.sendAlert("Purchase Order Disetujui: " + saved.getPoNumber() 
                + " oleh " + username + ". Siap untuk diproses/received di gudang.");

        return mapResponse(saved);
    }

    @Transactional
    public PurchaseApprovalResponse reject(Long purchaseOrderId, String reason) {
        PurchaseOrder purchaseOrder = requirePendingOrder(purchaseOrderId);
        User approver = currentUserService.requireCurrentUser();

        // 1. User yang membuat pengajuan tidak boleh approve/reject pengajuannya sendiri
        if (purchaseOrder.getCreatedBy() != null && purchaseOrder.getCreatedBy().getId().equals(approver.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        // 2. Hanya MANAGEMENT yang boleh menolak
        if (!approver.getRole().getName().equalsIgnoreCase("MANAGEMENT")) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED.name());
        purchaseOrder.setApprovedBy(approver);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setRejectionReason(reason.trim());

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        // Record Audit Log and Send Notification
        String username = approver.getUsername();
        activityLogService.record("REJECT_PURCHASE_ORDER", 
                "Rejected Purchase Order: " + saved.getPoNumber() + " by " + username + ". Reason: " + reason,
                "PURCHASE_ORDER", saved.getId());
        notificationService.sendAlert("Purchase Order Ditolak: " + saved.getPoNumber() 
                + " oleh " + username + ". Alasan: " + reason);

        return mapResponse(saved);
    }

    private PurchaseOrder requirePendingOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase Order tidak ditemukan dengan ID: " + purchaseOrderId
                ));

        if (!PurchaseOrderStatus.PENDING_APPROVAL.name().equals(purchaseOrder.getStatus())) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException(
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
