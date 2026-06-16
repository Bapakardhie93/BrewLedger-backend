package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse;
import com.brewledger.brewledger.backend.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderItemResponse;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderDetailResponse;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @GetMapping("/{id}")
    public PurchaseOrderDetailResponse findById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @GetMapping("/{id}/items")
    public List<PurchaseOrderItemResponse> getItems(
            @PathVariable Long id
    ) {

        return service.getItems(id);
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('GUDANG', 'MANAGEMENT')")
    public PurchaseOrderResponse receive(
            @PathVariable Long id
    ) {

        return service.receive(id);
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('GUDANG', 'MANAGEMENT')")
    public PurchaseOrderItemResponse addItem(
            @PathVariable Long id,
            @Valid
            @RequestBody CreatePurchaseOrderItemRequest request
    ) {

        return service.addItem(
                id,
                request
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GUDANG', 'MANAGEMENT')")
    public PurchaseOrderResponse create(
            @Valid
            @RequestBody CreatePurchaseOrderRequest request
    ) {

        return service.create(request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('GUDANG', 'MANAGEMENT')")
    public PurchaseOrderResponse submitForApproval(@PathVariable Long id) {
        return service.submitForApproval(id);
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {

        return service.findAll();
    }
}
