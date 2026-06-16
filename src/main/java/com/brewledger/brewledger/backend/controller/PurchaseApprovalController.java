package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.warehouse.PurchaseApprovalResponse;
import com.brewledger.brewledger.backend.dto.warehouse.RejectPurchaseOrderRequest;
import com.brewledger.brewledger.backend.service.PurchaseApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/approvals/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT')")
public class PurchaseApprovalController {

    private final PurchaseApprovalService purchaseApprovalService;

    @GetMapping
    public List<PurchaseApprovalResponse> findPendingApprovals() {
        return purchaseApprovalService.findPendingApprovals();
    }

    @PostMapping("/{id}/approve")
    public PurchaseApprovalResponse approve(@PathVariable Long id) {
        return purchaseApprovalService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public PurchaseApprovalResponse reject(
            @PathVariable Long id,
            @RequestBody RejectPurchaseOrderRequest request
    ) {
        String effectiveReason = request.getEffectiveReason();
        if (effectiveReason.isEmpty()) {
            throw new com.brewledger.brewledger.backend.exception.BusinessException("Alasan penolakan (reason/rejectReason) wajib diisi");
        }
        return purchaseApprovalService.reject(id, effectiveReason);
    }
}
