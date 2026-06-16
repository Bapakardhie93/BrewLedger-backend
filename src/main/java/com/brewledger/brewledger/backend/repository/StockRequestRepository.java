package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.StockRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {

    List<StockRequest> findAllByOrderByRequestedAtDesc();

    boolean existsByIngredientId(Long ingredientId);
}
