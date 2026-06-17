package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest;
import com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse;
import com.brewledger.brewledger.backend.dto.ingredient.UpdateIngredientRequest;
import com.brewledger.brewledger.backend.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.brewledger.brewledger.backend.dto.ingredient.LowStockResponse;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
public class IngredientController {

    private final IngredientService service;
    private final com.brewledger.brewledger.backend.service.ApprovalRequestService approvalRequestService;

    @PostMapping("/submit-new")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public com.brewledger.brewledger.backend.dto.warehouse.ApprovalResponse submitNewIngredient(
            @Valid @RequestBody CreateIngredientRequest request
    ) {
        return approvalRequestService.submitNewIngredient(request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public IngredientResponse create(
            @Valid
            @RequestBody CreateIngredientRequest request
    ) {
        return service.create(request);
    }
    @GetMapping("/low-stock")
    public List<LowStockResponse> getLowStock() {

        return service.getLowStock();
    }

    @GetMapping
    public List<IngredientResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/search")
    public List<IngredientResponse> search(
            @RequestParam String keyword
    ) {

        return service.search(
                keyword
        );
    }

    @GetMapping("/{id}")
    public IngredientResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public IngredientResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public IngredientResponse activate(@PathVariable Long id) {
        return service.toggleActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'GUDANG')")
    public IngredientResponse deactivate(@PathVariable Long id) {
        return service.toggleActive(id, false);
    }
}
