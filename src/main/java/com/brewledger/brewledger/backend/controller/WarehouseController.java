package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest;
import com.brewledger.brewledger.backend.dto.warehouse.UpdateWarehouseIngredientRequest;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseIngredientResponse;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseResponse;
import com.brewledger.brewledger.backend.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GUDANG', 'MANAGEMENT')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public WarehouseResponse getWorkspace(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return warehouseService.getWorkspace(keyword);
    }

    @PatchMapping("/ingredients/{id}")
    public WarehouseIngredientResponse updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseIngredientRequest request
    ) {
        return warehouseService.updateIngredient(id, request);
    }

    @PostMapping("/ingredients/{id}/adjust-stock")
    public WarehouseIngredientResponse adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return warehouseService.adjustStock(id, request);
    }
}
