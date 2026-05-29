package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse;
import com.brewledger.brewledger.backend.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderItemResponse;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @GetMapping("/{id}/items")
    public List<PurchaseOrderItemResponse> getItems(
            @PathVariable Long id
    ) {

        return service.getItems(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(
            @PathVariable Long id
    ) {

        return service.receive(id);
    }

    @PostMapping("/{id}/items")
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
    public PurchaseOrderResponse create(
            @Valid
            @RequestBody CreatePurchaseOrderRequest request
    ) {

        return service.create(request);
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {

        return service.findAll();
    }
}