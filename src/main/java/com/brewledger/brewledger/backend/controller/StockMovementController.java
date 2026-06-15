package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.stockmovement.StockMovementResponse;
import com.brewledger.brewledger.backend.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'GUDANG')")
public class StockMovementController {

    private final StockMovementService service;

    @GetMapping
    public List<StockMovementResponse> findAll() {

        return service.findAll();
    }
}
