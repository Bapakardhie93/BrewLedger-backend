package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.dto.dashboard.TopSellingProductResponse;
import com.brewledger.brewledger.backend.entity.TransactionItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionItemRepository
        extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransactionId(
            Long transactionId
    );

    @Query("""
           SELECT new com.brewledger.brewledger.backend.dto.dashboard.TopSellingProductResponse(
               ti.productName,
               SUM(ti.quantity),
               SUM(ti.subtotal)
           )
           FROM TransactionItem ti
           GROUP BY ti.productName
           ORDER BY SUM(ti.quantity) DESC
           """)
    List<TopSellingProductResponse> findTopSellingProducts(Pageable pageable);

    @Query("""
           SELECT ti
           FROM TransactionItem ti
           WHERE ti.transaction.createdAt BETWEEN :start AND :end
           """)
    List<TransactionItem> findByTransactionDateRange(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end
    );
}