package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.dashboard.DashboardResponse;
import com.brewledger.brewledger.backend.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardResponse getDashboard() {

        return service.getDashboard();
    }
}