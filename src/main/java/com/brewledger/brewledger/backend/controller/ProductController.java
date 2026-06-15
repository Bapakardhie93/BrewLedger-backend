package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.product.CreateProductRequest;
import com.brewledger.brewledger.backend.dto.product.ProductResponse;
import com.brewledger.brewledger.backend.dto.product.UpdateProductRequest;
import com.brewledger.brewledger.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    @GetMapping("/search")
    public List<ProductResponse> search(
            @RequestParam String keyword
    ) {

        return service.search(
                keyword
        );
    }

    private final ProductService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ProductResponse create(
            @Valid
            @RequestBody CreateProductRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return service.findAll();
    }


}
