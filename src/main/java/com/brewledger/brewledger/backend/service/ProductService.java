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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {

        if (productRepository.existsByCode(request.getCode())) {
            throw new BusinessException(
                    "Kode produk sudah digunakan: " + request.getCode()
            );
        }

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + request.getCategoryId()
                ));

        Product product = new Product();
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(category);
        product.setSellingPrice(request.getSellingPrice());
        product.setDescription(request.getDescription());

        productRepository.save(product);

        return mapToResponse(product);
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

        return mapToResponse(productRepository.save(product));
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

        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                categoryName,
                product.getSellingPrice(),
                product.getDescription(),
                product.getActive()
        );
    }
}
