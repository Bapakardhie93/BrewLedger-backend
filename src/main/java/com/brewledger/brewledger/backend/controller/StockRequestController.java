package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.stockrequest.CreateStockRequest;
import com.brewledger.brewledger.backend.dto.stockrequest.StockRequestResponse;
import com.brewledger.brewledger.backend.service.StockRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class StockRequestController {

    private final StockRequestService stockRequestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public StockRequestResponse create(
            @Valid @RequestBody CreateStockRequest request
    ) {
        return stockRequestService.create(request);
    }

    @GetMapping
    public List<StockRequestResponse> findAll(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String targetRole
    ) {
        return stockRequestService.findAll(targetRole);
    }

    public List<StockRequestResponse> findAll() {
        return findAll(null);
    }

    @PatchMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public StockRequestResponse process(@PathVariable Long id) {
        return stockRequestService.process(id);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public StockRequestResponse complete(@PathVariable Long id) {
        return stockRequestService.complete(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public StockRequestResponse reject(
            @PathVariable Long id,
            @RequestBody com.brewledger.brewledger.backend.dto.approval.RejectApprovalRequest request
    ) {
        String effectiveReason = request.getEffectiveReason();
        if (effectiveReason.isEmpty()) {
            throw new com.brewledger.brewledger.backend.exception.BusinessException("Alasan penolakan (reason/rejectReason) wajib diisi");
        }
        return stockRequestService.reject(id, effectiveReason);
    }
}
