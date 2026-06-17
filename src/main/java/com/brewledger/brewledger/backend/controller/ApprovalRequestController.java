package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.warehouse.ApprovalResponse;
import com.brewledger.brewledger.backend.dto.warehouse.RejectApprovalRequest;
import com.brewledger.brewledger.backend.service.ApprovalRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    @GetMapping
    public List<ApprovalResponse> findAll(
            @RequestParam(required = false) String targetRole
    ) {
        return approvalRequestService.findAll(targetRole);
    }

    public List<ApprovalResponse> findAll() {
        return findAll(null);
    }

    @GetMapping("/{id}")
    public ApprovalResponse findById(@PathVariable Long id) {
        return approvalRequestService.findById(id);
    }

    @PostMapping("/{id}/approve")
    public ApprovalResponse approveRequest(@PathVariable Long id) {
        return approvalRequestService.approveRequest(id);
    }

    @PostMapping("/{id}/reject")
    public ApprovalResponse rejectRequest(
            @PathVariable Long id,
            @RequestBody RejectApprovalRequest request
    ) {
        return approvalRequestService.rejectRequest(id, request);
    }
}
