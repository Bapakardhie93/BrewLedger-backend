package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.dto.dashboard.StockMovementSummaryResponse;
import com.brewledger.brewledger.backend.entity.StockMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
           SELECT sm
           FROM StockMovement sm
           JOIN FETCH sm.ingredient
           ORDER BY sm.movementDate DESC, sm.createdAt DESC
           """)
    List<StockMovement> findRecentMovements(Pageable pageable);

    @Query("""
           SELECT new com.brewledger.brewledger.backend.dto.dashboard.StockMovementSummaryResponse(
               i.name,
               SUM(CASE WHEN sm.movementType IN ('PURCHASE', 'IN') THEN sm.quantity ELSE 0.0 END),
               SUM(CASE WHEN sm.movementType IN ('SALE', 'OUT') THEN sm.quantity ELSE 0.0 END),
               SUM(sm.quantity),
               COUNT(sm)
           )
           FROM StockMovement sm
           JOIN sm.ingredient i
           GROUP BY i.id, i.name
           ORDER BY SUM(sm.quantity) DESC
           """)
    List<StockMovementSummaryResponse> findTopMovingIngredients(Pageable pageable);
}