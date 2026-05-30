package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.dashboard.DashboardResponse;
import com.brewledger.brewledger.backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final TransactionRepository transactionRepository;
    private final StockMovementRepository stockMovementRepository;

    public DashboardResponse getDashboard() {

        return new DashboardResponse(
                productRepository.count(),
                ingredientRepository.count(),
                supplierRepository.count(),
                transactionRepository.count(),
                transactionRepository.getTotalSales(),
                stockMovementRepository.count()
        );
    }
}