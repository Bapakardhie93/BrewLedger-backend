package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest;
import com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse;
import com.brewledger.brewledger.backend.dto.ingredient.LowStockResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStock() {

        return ingredientRepository
                .findLowStock()
                .stream()
                .map(ingredient -> new LowStockResponse(
                        ingredient.getId(),
                        ingredient.getCode(),
                        ingredient.getName(),
                        ingredient.getCurrentStock(),
                        ingredient.getMinimumStock()
                ))
                .toList();
    }

    @Transactional
    public IngredientResponse create(CreateIngredientRequest request) {

        if (ingredientRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "Kode ingredient sudah digunakan: " + request.getCode()
            );
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + request.getSupplierId()
                ));

        Ingredient ingredient = new Ingredient();
        ingredient.setCode(request.getCode());
        ingredient.setName(request.getName());
        ingredient.setSupplier(supplier);
        ingredient.setUnit(request.getUnit());
        ingredient.setCurrentStock(0.0);
        ingredient.setMinimumStock(request.getMinimumStock());
        ingredient.setCostPrice(request.getCostPrice());

        ingredientRepository.save(ingredient);

        return mapToResponse(ingredient);
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> findAll() {

        return ingredientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> search(String keyword) {

        return ingredientRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Bug Fix #5: Safely handles null supplier to avoid NullPointerException
     * on data that may not have an associated supplier.
     */
    private IngredientResponse mapToResponse(Ingredient ingredient) {
        String supplierName = (ingredient.getSupplier() != null)
                ? ingredient.getSupplier().getName()
                : "—";

        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getCode(),
                ingredient.getName(),
                supplierName,
                ingredient.getUnit(),
                ingredient.getCurrentStock(),
                ingredient.getMinimumStock(),
                ingredient.getCostPrice(),
                ingredient.getActive()
        );
    }
}