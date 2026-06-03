package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.product.CreateProductRequest;
import com.brewledger.brewledger.backend.dto.product.ProductResponse;
import com.brewledger.brewledger.backend.dto.product.UpdateProductRequest;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import com.brewledger.brewledger.backend.repository.TransactionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final com.brewledger.brewledger.backend.repository.ProductRecipeRepository productRecipeRepository;
    private final com.brewledger.brewledger.backend.repository.IngredientRepository ingredientRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {

        if (productRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "Kode produk sudah digunakan: " + request.getCode()
            );
        }

        Long categoryId = java.util.Objects.requireNonNull(request.getCategoryId(), "Category ID must not be null");
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + request.getCategoryId()
                ));

        Product product = new Product();
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(category);
        product.setSellingPrice(request.getSellingPrice());
        product.setDescription(request.getDescription());
        
        product.setUseCustomHpp(request.getUseCustomHpp() != null ? request.getUseCustomHpp() : false);
        product.setCustomHpp(request.getCustomHpp() != null ? request.getCustomHpp() : 0.0);
        product.setMargin(request.getMargin() != null ? request.getMargin() : 0.0);

        Product savedProduct = productRepository.save(product);

        if (request.getRecipeItems() != null) {
            for (com.brewledger.brewledger.backend.dto.product.RecipeItemRequest item : request.getRecipeItems()) {
                com.brewledger.brewledger.backend.entity.Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient tidak ditemukan dengan ID: " + item.getIngredientId()));
                com.brewledger.brewledger.backend.entity.ProductRecipe recipe = new com.brewledger.brewledger.backend.entity.ProductRecipe();
                recipe.setProduct(savedProduct);
                recipe.setIngredient(ingredient);
                recipe.setQuantityRequired(item.getQuantityRequired());
                productRecipeRepository.save(recipe);
            }
        }

        activityLogService.record("CREATE_PRODUCT", 
                "Created product: " + savedProduct.getName() + " (" + savedProduct.getCode() + ") with price " + savedProduct.getSellingPrice(),
                "PRODUCT", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produk tidak ditemukan dengan ID: " + id
                ));

        if (productRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException(
                    "Kode produk sudah digunakan: " + request.getCode()
            );
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + request.getCategoryId()
                ));

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(category);
        product.setSellingPrice(request.getSellingPrice());
        product.setDescription(request.getDescription());
        product.setActive(request.getActive());
        
        product.setUseCustomHpp(request.getUseCustomHpp() != null ? request.getUseCustomHpp() : false);
        product.setCustomHpp(request.getCustomHpp() != null ? request.getCustomHpp() : 0.0);
        product.setMargin(request.getMargin() != null ? request.getMargin() : 0.0);

        if (request.getRecipeItems() != null) {
            productRecipeRepository.deleteByProductId(product.getId());
            for (com.brewledger.brewledger.backend.dto.product.RecipeItemRequest item : request.getRecipeItems()) {
                com.brewledger.brewledger.backend.entity.Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient tidak ditemukan dengan ID: " + item.getIngredientId()));
                com.brewledger.brewledger.backend.entity.ProductRecipe recipe = new com.brewledger.brewledger.backend.entity.ProductRecipe();
                recipe.setProduct(product);
                recipe.setIngredient(ingredient);
                recipe.setQuantityRequired(item.getQuantityRequired());
                productRecipeRepository.save(recipe);
            }
        }

        Product saved = productRepository.save(product);
        activityLogService.record("UPDATE_PRODUCT", 
                "Updated product: " + saved.getName() + " (" + saved.getCode() + ")",
                "PRODUCT", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String keyword) {

        return productRepository
                .findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Safely maps a product to its response, handling null category to prevent NullPointerException.
     */
    private ProductResponse mapToResponse(Product product) {
        String categoryName = (product.getCategory() != null)
                ? product.getCategory().getName()
                : "—";

        java.util.List<com.brewledger.brewledger.backend.entity.ProductRecipe> recipes = productRecipeRepository.findByProductId(product.getId());
        double calculatedHpp = 0.0;
        java.util.List<com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse> recipeItems = new java.util.ArrayList<>();
        for (com.brewledger.brewledger.backend.entity.ProductRecipe recipe : recipes) {
            calculatedHpp += recipe.getQuantityRequired() * (recipe.getIngredient().getCostPrice() != null ? recipe.getIngredient().getCostPrice() : 0.0);
            recipeItems.add(new com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse(
                    recipe.getId(),
                    product.getName(),
                    recipe.getIngredient().getName(),
                    recipe.getQuantityRequired()
            ));
        }

        boolean useCustom = Boolean.TRUE.equals(product.getUseCustomHpp());
        double customHpp = product.getCustomHpp() != null ? product.getCustomHpp() : 0.0;
        double hpp = useCustom ? customHpp : calculatedHpp;
        double margin = product.getMargin() != null ? product.getMargin() : 0.0;
        
        double recommendedSellingPrice;
        if (margin < 100.0) {
            recommendedSellingPrice = hpp / (1.0 - (margin / 100.0));
        } else {
            recommendedSellingPrice = hpp * (1.0 + (margin / 100.0));
        }

        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                categoryName,
                product.getSellingPrice(),
                product.getDescription(),
                product.getActive(),
                useCustom,
                customHpp,
                calculatedHpp,
                hpp,
                margin,
                recommendedSellingPrice,
                recipeItems
        );
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produk tidak ditemukan dengan ID: " + id
                ));

        if (transactionItemRepository.existsByProductId(id)) {
            throw new BusinessException(
                    "Produk tidak dapat dihapus karena sudah digunakan dalam transaksi"
            );
        }

        productRepository.delete(product);
        activityLogService.record("DELETE_PRODUCT", 
                "Deleted product: " + product.getName() + " (" + product.getCode() + ")",
                "PRODUCT", product.getId());
    }

    @Transactional
    public ProductResponse toggleActive(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produk tidak ditemukan dengan ID: " + id
                ));

        product.setActive(active);
        Product saved = productRepository.save(product);
        activityLogService.record("TOGGLE_PRODUCT_ACTIVE", 
                "Toggled active status of product: " + saved.getName() + " to " + active,
                "PRODUCT", saved.getId());
        return mapToResponse(saved);
    }
}
