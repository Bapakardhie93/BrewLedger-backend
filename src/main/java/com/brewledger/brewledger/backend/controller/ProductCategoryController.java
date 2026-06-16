package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.category.CategoryResponse;
import com.brewledger.brewledger.backend.dto.category.CreateCategoryRequest;
import com.brewledger.brewledger.backend.dto.category.UpdateCategoryRequest;
import com.brewledger.brewledger.backend.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT')")
public class ProductCategoryController {

    private final ProductCategoryService service;

    @PostMapping
    public CategoryResponse create(
            @RequestBody CreateCategoryRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<CategoryResponse> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGEMENT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/activate")
    public CategoryResponse activate(@PathVariable Long id) {
        return service.toggleActive(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    public CategoryResponse deactivate(@PathVariable Long id) {
        return service.toggleActive(id, false);
    }
}
