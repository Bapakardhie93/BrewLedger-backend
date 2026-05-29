package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionItemRepository
        extends JpaRepository<TransactionItem, Long> {

    List<TransactionItem> findByTransactionId(
            Long transactionId
    );
}