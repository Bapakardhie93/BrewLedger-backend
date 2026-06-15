package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.stockrequest.CreateStockRequest;
import com.brewledger.brewledger.backend.dto.stockrequest.StockRequestResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.StockRequest;
import com.brewledger.brewledger.backend.entity.StockRequestStatus;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.StockRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockRequestService {

    private final StockRequestRepository stockRequestRepository;
    private final IngredientRepository ingredientRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public StockRequestResponse create(CreateStockRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + request.getIngredientId()
                ));

        if (!Boolean.TRUE.equals(ingredient.getActive())) {
            throw new BusinessException("Ingredient tidak aktif dan tidak dapat diminta");
        }

        LocalDateTime now = LocalDateTime.now();
        StockRequest stockRequest = new StockRequest();
        stockRequest.setRequestNumber("SR-" + UUID.randomUUID().toString().toUpperCase());
        stockRequest.setIngredient(ingredient);
        stockRequest.setRequestedQuantity(request.getRequestedQuantity());
        stockRequest.setNotes(request.getNotes());
        stockRequest.setStatus(StockRequestStatus.REQUESTED);
        stockRequest.setRequestedBy(currentUserService.requireCurrentUser());
        stockRequest.setRequestedAt(now);

        return mapToResponse(stockRequestRepository.save(stockRequest));
    }

    @Transactional(readOnly = true)
    public List<StockRequestResponse> findAll() {
        return stockRequestRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StockRequestResponse process(Long id) {
        StockRequest stockRequest = requireRequest(id);
        requireStatus(stockRequest, StockRequestStatus.REQUESTED);

        User processor = currentUserService.requireCurrentUser();
        stockRequest.setStatus(StockRequestStatus.PROCESSING);
        stockRequest.setProcessedBy(processor);
        stockRequest.setProcessedAt(LocalDateTime.now());

        return mapToResponse(stockRequestRepository.save(stockRequest));
    }

    @Transactional
    public StockRequestResponse complete(Long id) {
        StockRequest stockRequest = requireRequest(id);
        requireStatus(stockRequest, StockRequestStatus.PROCESSING);

        if (stockRequest.getProcessedBy() == null) {
            stockRequest.setProcessedBy(currentUserService.requireCurrentUser());
            stockRequest.setProcessedAt(LocalDateTime.now());
        }

        stockRequest.setStatus(StockRequestStatus.COMPLETED);
        stockRequest.setCompletedAt(LocalDateTime.now());

        return mapToResponse(stockRequestRepository.save(stockRequest));
    }

    private StockRequest requireRequest(Long id) {
        return stockRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock request tidak ditemukan dengan ID: " + id
                ));
    }

    private void requireStatus(StockRequest stockRequest, StockRequestStatus expectedStatus) {
        if (stockRequest.getStatus() != expectedStatus) {
            throw new BusinessException(
                    "Stock request harus berstatus " + expectedStatus
                            + ". Status saat ini: " + stockRequest.getStatus()
            );
        }
    }

    private StockRequestResponse mapToResponse(StockRequest stockRequest) {
        return new StockRequestResponse(
                stockRequest.getId(),
                stockRequest.getRequestNumber(),
                stockRequest.getIngredient().getId(),
                stockRequest.getIngredient().getCode(),
                stockRequest.getIngredient().getName(),
                stockRequest.getIngredient().getUnit(),
                stockRequest.getRequestedQuantity(),
                stockRequest.getNotes(),
                stockRequest.getStatus().name(),
                stockRequest.getRequestedBy().getFullName(),
                stockRequest.getRequestedAt(),
                stockRequest.getProcessedBy() != null
                        ? stockRequest.getProcessedBy().getFullName()
                        : null,
                stockRequest.getProcessedAt(),
                stockRequest.getCompletedAt()
        );
    }
}
