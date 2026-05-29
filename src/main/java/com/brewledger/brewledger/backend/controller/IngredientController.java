package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest;
import com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse;
import com.brewledger.brewledger.backend.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @PostMapping
    public IngredientResponse create(
            @Valid
            @RequestBody CreateIngredientRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<IngredientResponse> findAll() {
        return service.findAll();
    }
}