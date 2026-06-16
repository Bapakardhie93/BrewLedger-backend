package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.kitchen.KitchenOrderResponse;
import com.brewledger.brewledger.backend.dto.kitchen.UpdateKitchenOrderStatusRequest;
import com.brewledger.brewledger.backend.service.KitchenOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen/orders")
@RequiredArgsConstructor
public class KitchenOrderController {

    private final KitchenOrderService kitchenOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public List<KitchenOrderResponse> findAll(
            @RequestParam(required = false) String cashier
    ) {
        return kitchenOrderService.findAll(cashier);
    }

    public List<KitchenOrderResponse> findAll() {
        return findAll(null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public KitchenOrderResponse findById(@PathVariable Long id) {
        return kitchenOrderService.findById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public KitchenOrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKitchenOrderStatusRequest request
    ) {
        return kitchenOrderService.updateStatus(id, request);
    }
}
