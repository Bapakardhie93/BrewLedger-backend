package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Product> findByNameContainingIgnoreCase(
            String keyword
    );

    List<Product> findByActiveTrueOrderByNameAsc();

    @Query("""
           SELECT p
           FROM Product p
           WHERE p.active = true
             AND (
                 LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
             )
           ORDER BY p.name
           """)
    List<Product> findActiveByNameOrCode(@Param("keyword") String keyword);
}
