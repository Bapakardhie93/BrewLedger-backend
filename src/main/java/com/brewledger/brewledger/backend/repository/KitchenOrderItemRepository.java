package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.KitchenOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitchenOrderItemRepository extends JpaRepository<KitchenOrderItem, Long> {
}
