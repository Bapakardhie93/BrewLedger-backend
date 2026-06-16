package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.category.CategoryResponse;
import com.brewledger.brewledger.backend.dto.category.CreateCategoryRequest;
import com.brewledger.brewledger.backend.dto.category.UpdateCategoryRequest;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final ProductRepository productRepository;

    public CategoryResponse create(
            CreateCategoryRequest request
    ) {

        if (repository.existsByName(request.getName())) {
            throw new BusinessException(
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

    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        ProductCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + id
                ));

        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException(
                    "Kategori dengan nama '" + request.getName() + "' sudah ada"
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        repository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }

    public void delete(Long id) {
        ProductCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + id
                ));

        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException(
                    "Kategori tidak dapat dihapus karena masih digunakan oleh produk"
            );
        }

        repository.delete(category);
    }

    public CategoryResponse toggleActive(Long id, boolean active) {
        ProductCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategori tidak ditemukan dengan ID: " + id
                ));

        category.setActive(active);
        repository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getActive()
        );
    }
}