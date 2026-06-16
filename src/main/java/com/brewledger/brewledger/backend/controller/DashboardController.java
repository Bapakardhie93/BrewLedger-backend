package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.dashboard.DashboardResponse;
import com.brewledger.brewledger.backend.dto.dashboard.TopSellingProductResponse;
import com.brewledger.brewledger.backend.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT')")
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardResponse getDashboard() {

        return service.getDashboard();
    }

    @GetMapping("/best-products")
    public List<TopSellingProductResponse> getBestProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getTopSellingProducts(limit);
    }
}
