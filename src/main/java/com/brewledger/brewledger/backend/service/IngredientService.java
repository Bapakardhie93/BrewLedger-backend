package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest;
import com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse;
import com.brewledger.brewledger.backend.dto.ingredient.LowStockResponse;
import com.brewledger.brewledger.backend.dto.ingredient.UpdateIngredientRequest;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockRequestRepository stockRequestRepository;
    private final ActivityLogService activityLogService;

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
        if (request.getPurchasePrice() != null && request.getPurchasePrice() < 0.0) {
            throw new BusinessException("Harga pembelian (purchasePrice) tidak boleh negatif");
        }
        if (request.getPackSize() != null && request.getPackSize() <= 0.0) {
            throw new BusinessException("Ukuran kemasan (packSize) harus lebih besar dari 0");
        }
        if (request.getCostPrice() != null && request.getCostPrice() < 0.0) {
            throw new BusinessException("Harga pokok (costPrice) tidak boleh negatif");
        }

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
        
        double purchase = (request.getPurchasePrice() != null) ? request.getPurchasePrice() : (request.getCostPrice() != null ? request.getCostPrice() : 0.0);
        double size = (request.getPackSize() != null && request.getPackSize() > 0.0) ? request.getPackSize() : 1.0;
        ingredient.setPurchasePrice(purchase);
        ingredient.setPackSize(size);
        ingredient.setCostPrice(purchase / size);

        Ingredient saved = ingredientRepository.save(ingredient);
        activityLogService.record("CREATE_INGREDIENT", 
                "Created ingredient: " + saved.getName() + " (" + saved.getCode() + ")",
                "INGREDIENT", saved.getId());

        return mapToResponse(saved);
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
                ingredient.getPurchasePrice() != null ? ingredient.getPurchasePrice() : 0.0,
                ingredient.getPackSize() != null ? ingredient.getPackSize() : 1.0,
                ingredient.getActive()
        );
    }

    @Transactional(readOnly = true)
    public IngredientResponse findById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + id
                ));
        return mapToResponse(ingredient);
    }

    @Transactional
    public IngredientResponse update(Long id, UpdateIngredientRequest request) {
        if (request.getPurchasePrice() != null && request.getPurchasePrice() < 0.0) {
            throw new BusinessException("Harga pembelian (purchasePrice) tidak boleh negatif");
        }
        if (request.getPackSize() != null && request.getPackSize() <= 0.0) {
            throw new BusinessException("Ukuran kemasan (packSize) harus lebih besar dari 0");
        }
        if (request.getCostPrice() != null && request.getCostPrice() < 0.0) {
            throw new BusinessException("Harga pokok (costPrice) tidak boleh negatif");
        }

        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + id
                ));

        if (ingredientRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException(
                    "Kode ingredient sudah digunakan: " + request.getCode()
            );
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + request.getSupplierId()
                ));

        ingredient.setCode(request.getCode());
        ingredient.setName(request.getName());
        ingredient.setSupplier(supplier);
        ingredient.setUnit(request.getUnit());
        ingredient.setMinimumStock(request.getMinimumStock());
        
        Double purchase = request.getPurchasePrice();
        Double size = request.getPackSize();
        if (purchase == null) {
            purchase = (request.getCostPrice() != null) ? request.getCostPrice() : (ingredient.getPurchasePrice() != null ? ingredient.getPurchasePrice() : 0.0);
        }
        if (size == null) {
            size = (ingredient.getPackSize() != null) ? ingredient.getPackSize() : 1.0;
        }
        ingredient.setPurchasePrice(purchase);
        ingredient.setPackSize(size);
        ingredient.setCostPrice(purchase / (size > 0.0 ? size : 1.0));

        if (request.getActive() != null) {
            ingredient.setActive(request.getActive());
        }

        Ingredient saved = ingredientRepository.save(ingredient);
        activityLogService.record("UPDATE_INGREDIENT", 
                "Updated ingredient: " + saved.getName() + " (" + saved.getCode() + ")",
                "INGREDIENT", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + id
                ));

        if (productRecipeRepository.existsByIngredientId(id)) {
            throw new BusinessException(
                    "Ingredient tidak dapat dihapus karena masih digunakan dalam resep produk"
            );
        }

        if (purchaseOrderItemRepository.existsByIngredientId(id)) {
            throw new BusinessException(
                    "Ingredient tidak dapat dihapus karena tercatat pada item pembelian"
            );
        }

        if (stockRequestRepository.existsByIngredientId(id)) {
            throw new BusinessException(
                    "Ingredient tidak dapat dihapus karena tercatat pada pengajuan stok"
            );
        }

        ingredientRepository.delete(ingredient);
        activityLogService.record("DELETE_INGREDIENT", 
                "Deleted ingredient: " + ingredient.getName() + " (" + ingredient.getCode() + ")",
                "INGREDIENT", ingredient.getId());
    }

    @Transactional
    public IngredientResponse toggleActive(Long id, boolean active) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + id
                ));

        ingredient.setActive(active);
        Ingredient saved = ingredientRepository.save(ingredient);
        activityLogService.record("TOGGLE_INGREDIENT_ACTIVE", 
                "Toggled active status of ingredient: " + saved.getName() + " to " + active,
                "INGREDIENT", saved.getId());
        return mapToResponse(saved);
    }
}