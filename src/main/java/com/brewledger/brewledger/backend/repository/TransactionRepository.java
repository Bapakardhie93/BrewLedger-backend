package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    @Query("""
           SELECT COALESCE(SUM(t.total), 0)
           FROM Transaction t
           """)
    Double getTotalSales();
}