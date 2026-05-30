package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest;
import com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.brewledger.brewledger.backend.dto.ingredient.LowStockResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;

    public List<LowStockResponse> getLowStock() {

        return ingredientRepository
                .findLowStock()
                .stream()
                .map(ingredient ->
                        new LowStockResponse(
                                ingredient.getId(),
                                ingredient.getCode(),
                                ingredient.getName(),
                                ingredient.getCurrentStock(),
                                ingredient.getMinimumStock()
                        )
                )
                .toList();
    }

    public IngredientResponse create(
            CreateIngredientRequest request
    ) {

        if (ingredientRepository.existsByCode(
                request.getCode()
        )) {
            throw new RuntimeException(
                    "Kode ingredient sudah digunakan"
            );
        }

        Supplier supplier =
                supplierRepository.findById(
                                request.getSupplierId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supplier tidak ditemukan"
                                ));

        Ingredient ingredient =
                new Ingredient();

        ingredient.setCode(
                request.getCode()
        );

        ingredient.setName(
                request.getName()
        );

        ingredient.setSupplier(
                supplier
        );

        ingredient.setUnit(
                request.getUnit()
        );

        ingredient.setCurrentStock(
                0.0
        );

        ingredient.setMinimumStock(
                request.getMinimumStock()
        );

        ingredient.setCostPrice(
                request.getCostPrice()
        );

        ingredientRepository.save(
                ingredient
        );

        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getCode(),
                ingredient.getName(),
                supplier.getName(),
                ingredient.getUnit(),
                ingredient.getCurrentStock(),
                ingredient.getMinimumStock(),
                ingredient.getCostPrice(),
                ingredient.getActive()
        );
    }

    public List<IngredientResponse> findAll() {

        return ingredientRepository.findAll()
                .stream()
                .map(ingredient ->
                        new IngredientResponse(
                                ingredient.getId(),
                                ingredient.getCode(),
                                ingredient.getName(),
                                ingredient.getSupplier()
                                        .getName(),
                                ingredient.getUnit(),
                                ingredient.getCurrentStock(),
                                ingredient.getMinimumStock(),
                                ingredient.getCostPrice(),
                                ingredient.getActive()
                        )
                )
                .toList();
    }

    public List<IngredientResponse> search(
            String keyword
    ) {

        return ingredientRepository
                .findByNameContainingIgnoreCase(
                        keyword
                )
                .stream()
                .map(ingredient ->
                        new IngredientResponse(
                                ingredient.getId(),
                                ingredient.getCode(),
                                ingredient.getName(),
                                ingredient.getSupplier().getName(),
                                ingredient.getUnit(),
                                ingredient.getCurrentStock(),
                                ingredient.getMinimumStock(),
                                ingredient.getCostPrice(),
                                ingredient.getActive()
                        )
                )
                .toList();
    }
}