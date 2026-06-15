package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.supplier.CreateSupplierRequest;
import com.brewledger.brewledger.backend.dto.supplier.SupplierResponse;
import com.brewledger.brewledger.backend.dto.supplier.UpdateSupplierRequest;
import com.brewledger.brewledger.backend.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT', 'GUDANG')")
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public SupplierResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
