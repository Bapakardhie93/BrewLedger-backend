package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByPurchaseOrderId(
            Long purchaseOrderId
    );

    @org.springframework.data.jpa.repository.Query("""
           SELECT poi
           FROM PurchaseOrderItem poi
           WHERE poi.purchaseOrder.orderDate BETWEEN :start AND :end
           """)
    List<PurchaseOrderItem> findByPurchaseOrderDateRange(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDate start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDate end
    );

    boolean existsByIngredientId(Long ingredientId);
}