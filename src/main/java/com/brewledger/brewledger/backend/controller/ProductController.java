package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.product.CreateProductRequest;
import com.brewledger.brewledger.backend.dto.product.ProductResponse;
import com.brewledger.brewledger.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ProductResponse create(
            @Valid
            @RequestBody CreateProductRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<ProductResponse> findAll() {
        return service.findAll();
    }


}