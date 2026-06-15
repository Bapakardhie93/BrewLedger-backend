package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    long countByStatusIgnoreCase(String status);

    List<PurchaseOrder> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    @org.springframework.data.jpa.repository.Query("""
           SELECT po
           FROM PurchaseOrder po
           WHERE po.orderDate BETWEEN :start AND :end
           ORDER BY po.orderDate DESC
           """)
    List<PurchaseOrder> findByDateRange(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDate start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDate end
    );
}
