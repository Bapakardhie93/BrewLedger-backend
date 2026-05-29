package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.supplier.CreateSupplierRequest;
import com.brewledger.brewledger.backend.dto.supplier.SupplierResponse;
import com.brewledger.brewledger.backend.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    public SupplierResponse create(
            @Valid
            @RequestBody CreateSupplierRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<SupplierResponse> findAll() {
        return service.findAll();
    }
}