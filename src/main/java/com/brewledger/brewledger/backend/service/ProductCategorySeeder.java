package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Order(3)
@Component
public class ProductCategorySeeder implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategorySeeder(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List.of(
                defaultCategory(
                        "Makanan Berat",
                        "Menu utama seperti nasi, pasta, mie, dan hidangan utama lainnya"
                ),
                defaultCategory(
                        "Makanan Ringan",
                        "Camilan dan snack pendamping minuman",
                        "Snack"
                ),
                defaultCategory(
                        "Kopi",
                        "Minuman berbasis espresso, manual brew, dan kopi susu",
                        "Coffee"
                ),
                defaultCategory(
                        "Non Kopi",
                        "Minuman tanpa kopi seperti cokelat, matcha, susu, dan mocktail",
                        "Non-Kopi",
                        "Non Coffee"
                ),
                defaultCategory(
                        "Teh",
                        "Minuman berbasis teh, termasuk milk tea dan infused tea",
                        "Tea"
                ),
                defaultCategory(
                        "Dessert",
                        "Kue, pastry, es krim, dan hidangan penutup"
                ),
                defaultCategory(
                        "Paket/Bundling",
                        "Paket hemat atau kombinasi makanan dan minuman"
                ),
                defaultCategory(
                        "Merchandise",
                        "Produk non-konsumsi seperti tumbler, biji kopi kemasan, dan aksesori"
                )
        ).forEach(this::upsertCategory);
    }

    private DefaultCategory defaultCategory(String name, String description, String... legacyNames) {
        return new DefaultCategory(name, description, List.of(legacyNames));
    }

    private void upsertCategory(DefaultCategory defaultCategory) {
        ProductCategory category = findExistingCategory(defaultCategory)
                .orElseGet(ProductCategory::new);

        category.setName(defaultCategory.name());
        category.setDescription(defaultCategory.description());
        category.setActive(true);

        categoryRepository.save(category);
    }

    private java.util.Optional<ProductCategory> findExistingCategory(DefaultCategory defaultCategory) {
        return categoryRepository.findByName(defaultCategory.name())
                .or(() -> defaultCategory.legacyNames()
                        .stream()
                        .map(categoryRepository::findByName)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .findFirst()
                );
    }

    private record DefaultCategory(String name, String description, List<String> legacyNames) {
    }
}
