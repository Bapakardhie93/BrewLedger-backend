package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}