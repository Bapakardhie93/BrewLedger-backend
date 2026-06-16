package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.stockmovement.StockMovementResponse;
import com.brewledger.brewledger.backend.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public List<StockMovementResponse> findAll() {

        return stockMovementRepository.findAll()
                .stream()
                .map(movement ->
                        new StockMovementResponse(
                                movement.getId(),
                                movement.getIngredient().getId(),
                                movement.getIngredient().getName(),
                                movement.getMovementType(),
                                movement.getQuantity(),
                                movement.getStockBefore(),
                                movement.getStockAfter(),
                                movement.getReferenceNumber(),
                                movement.getMovementDate(),
                                movement.getCreatedBy()
                        )
                )
                .toList();
    }
}