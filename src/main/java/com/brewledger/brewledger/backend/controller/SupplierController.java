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
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
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
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public SupplierResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public SupplierResponse activate(@PathVariable Long id) {
        return service.toggleActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public SupplierResponse deactivate(@PathVariable Long id) {
        return service.toggleActive(id, false);
    }
}
