package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.product.CreateProductRequest;
import com.brewledger.brewledger.backend.dto.product.ProductResponse;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductResponse create(
            CreateProductRequest request
    ) {

        if (productRepository.existsByCode(
                request.getCode()
        )) {
            throw new RuntimeException(
                    "Kode produk sudah digunakan"
            );
        }

        ProductCategory category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Kategori tidak ditemukan"
                                ));

        Product product = new Product();

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(category);
        product.setSellingPrice(
                request.getSellingPrice()
        );
        product.setDescription(
                request.getDescription()
        );

        productRepository.save(product);

        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                category.getName(),
                product.getSellingPrice(),
                product.getDescription(),
                product.getActive()
        );
    }

    public List<ProductResponse> findAll() {

        return productRepository.findAll()
                .stream()
                .map(product ->
                        new ProductResponse(
                                product.getId(),
                                product.getCode(),
                                product.getName(),
                                product.getCategory()
                                        .getName(),
                                product.getSellingPrice(),
                                product.getDescription(),
                                product.getActive()
                        )
                )
                .toList();
    }
}