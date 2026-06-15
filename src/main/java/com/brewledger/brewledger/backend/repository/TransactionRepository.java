package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    @Query("""
           SELECT COALESCE(SUM(t.total), 0)
           FROM Transaction t
           """)
    Double getTotalSales();

    @Query("""
           SELECT t
           FROM Transaction t
           ORDER BY t.createdAt DESC
           """)
    List<Transaction> findRecentTransactions(Pageable pageable);

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.createdAt BETWEEN :start AND :end
           ORDER BY t.createdAt DESC
           """)
    List<Transaction> findByDateRange(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end
    );
}