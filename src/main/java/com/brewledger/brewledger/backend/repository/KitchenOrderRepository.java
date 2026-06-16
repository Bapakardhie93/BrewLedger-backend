package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.KitchenOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {
    List<KitchenOrder> findByTransactionId(Long transactionId);

    long countByTransactionCashierIdAndStatusIn(Long cashierId, List<com.brewledger.brewledger.backend.enums.KitchenOrderStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT ko FROM KitchenOrder ko WHERE ko.transaction.cashier.username = :username ORDER BY ko.createdAt DESC")
    List<KitchenOrder> findByCashierUsername(@org.springframework.data.repository.query.Param("username") String username);
}
