package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}