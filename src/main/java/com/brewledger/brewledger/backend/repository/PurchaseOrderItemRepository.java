package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
}