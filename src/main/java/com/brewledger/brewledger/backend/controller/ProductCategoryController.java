package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.category.CategoryResponse;
import com.brewledger.brewledger.backend.dto.category.CreateCategoryRequest;
import com.brewledger.brewledger.backend.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
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
}