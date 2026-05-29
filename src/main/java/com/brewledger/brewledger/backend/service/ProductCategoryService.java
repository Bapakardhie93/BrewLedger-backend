package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.category.CategoryResponse;
import com.brewledger.brewledger.backend.dto.category.CreateCategoryRequest;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    public CategoryResponse create(
            CreateCategoryRequest request
    ) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException(
                    "Kategori sudah ada"
            );
        }

        ProductCategory category =
                new ProductCategory();

        category.setName(request.getName());
        category.setDescription(
                request.getDescription()
        );

        repository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }

    public List<CategoryResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(category ->
                        new CategoryResponse(
                                category.getId(),
                                category.getName(),
                                category.getDescription(),
                                category.getActive()
                        )
                )
                .toList();
    }
}