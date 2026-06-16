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
import com.brewledger.brewledger.backend.repository.StockMovementRepository;
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
    private final StockMovementRepository stockMovementRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public StockRequestResponse create(CreateStockRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingredient tidak ditemukan dengan ID: " + request.getIngredientId()
                ));

        if (!Boolean.TRUE.equals(ingredient.getActive())) {
            throw new BusinessException("Ingredient tidak aktif and tidak dapat diminta");
        }

        User currentUser = currentUserService.requireCurrentUser();
        String roleName = currentUser.getRole().getName();
        String targetRole = roleName.equalsIgnoreCase("MANAGEMENT") ? "GUDANG" : "MANAGEMENT";

        LocalDateTime now = LocalDateTime.now();
        StockRequest stockRequest = new StockRequest();
        stockRequest.setRequestNumber("SR-" + UUID.randomUUID().toString().toUpperCase());
        stockRequest.setIngredient(ingredient);
        stockRequest.setRequestedQuantity(request.getRequestedQuantity());
        stockRequest.setNotes(request.getNotes());
        stockRequest.setStatus(StockRequestStatus.REQUESTED);
        stockRequest.setRequestedBy(currentUser);
        stockRequest.setRequestedByRole(roleName);
        stockRequest.setTargetRole(targetRole);
        stockRequest.setRequestedAt(now);

        StockRequest saved = stockRequestRepository.save(stockRequest);
        activityLogService.record("CREATE_STOCK_REQUEST", 
                "Created stock request: " + saved.getRequestNumber() + " for ingredient " + ingredient.getName() + " qty: " + saved.getRequestedQuantity(),
                "STOCK_REQUEST", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StockRequestResponse> findAll(String targetRole) {
        List<StockRequest> requests = stockRequestRepository.findAllByOrderByRequestedAtDesc();
        if (targetRole != null && !targetRole.trim().isEmpty()) {
            return requests.stream()
                    .filter(sr -> sr.getTargetRole() != null && sr.getTargetRole().equalsIgnoreCase(targetRole.trim()))
                    .map(this::mapToResponse)
                    .toList();
        }
        return requests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockRequestResponse> findAll() {
        return findAll(null);
    }
    private boolean hasRole(String roleName) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equalsIgnoreCase("ROLE_" + roleName));
        }
        return false;
    }

    private void validateStockRequestPermission(StockRequest request, User processor) {
        String requiredRole = request.getTargetRole();
        if (requiredRole != null && !hasRole(requiredRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        if (request.getRequestedBy() != null && request.getRequestedBy().getId().equals(processor.getId())) {
            if (request.getRequestedByRole() != null && hasRole(request.getRequestedByRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
            }
        }
    }



    @Transactional
    public StockRequestResponse process(Long id) {
        StockRequest stockRequest = requireRequest(id);
        if (stockRequest.getStatus() != StockRequestStatus.REQUESTED) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException(
                    "Stock request harus berstatus REQUESTED untuk diproses. Stock request hanya dapat diproses dari status REQUESTED atau APPROVED."
            );
        }

        User processor = currentUserService.requireCurrentUser();
        validateStockRequestPermission(stockRequest, processor);

        stockRequest.setStatus(StockRequestStatus.PROCESSING);
        stockRequest.setProcessedBy(processor);
        stockRequest.setProcessedAt(LocalDateTime.now());

        StockRequest saved = stockRequestRepository.save(stockRequest);
        activityLogService.record("PROCESS_STOCK_REQUEST", 
                "Processing stock request: " + saved.getRequestNumber() + " by " + processor.getUsername(),
                "STOCK_REQUEST", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public StockRequestResponse complete(Long id) {
        StockRequest stockRequest = requireRequest(id);
        if (stockRequest.getStatus() != StockRequestStatus.PROCESSING) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException(
                    "Stock request harus berstatus PROCESSING untuk diselesaikan. Stock request hanya dapat diselesaikan dari status PROCESSING."
            );
        }

        User processor = currentUserService.requireCurrentUser();
        validateStockRequestPermission(stockRequest, processor);

        if (stockRequest.getProcessedBy() == null) {
            stockRequest.setProcessedBy(processor);
            stockRequest.setProcessedAt(LocalDateTime.now());
        }

        // 1. Update stok bahan baku
        Ingredient ingredient = stockRequest.getIngredient();
        double stockBefore = ingredient.getCurrentStock() != null ? ingredient.getCurrentStock() : 0.0;
        double quantity = stockRequest.getRequestedQuantity();
        double stockAfter = stockBefore + quantity;
        ingredient.setCurrentStock(stockAfter);
        ingredientRepository.save(ingredient);

        // 2. Buat stock movement
        com.brewledger.brewledger.backend.entity.StockMovement movement = new com.brewledger.brewledger.backend.entity.StockMovement();
        movement.setIngredient(ingredient);
        movement.setQuantity(quantity);
        movement.setStockBefore(stockBefore);
        movement.setStockAfter(stockAfter);
        movement.setMovementType("STOCK_REQUEST_COMPLETED");
        movement.setReferenceNumber(stockRequest.getRequestNumber());
        movement.setMovementDate(LocalDateTime.now());
        movement.setCreatedBy(processor.getUsername());
        stockMovementRepository.save(movement);

        // 3. Ubah status request ke COMPLETED
        stockRequest.setStatus(StockRequestStatus.COMPLETED);
        stockRequest.setCompletedAt(LocalDateTime.now());

        StockRequest saved = stockRequestRepository.save(stockRequest);
        activityLogService.record("COMPLETE_STOCK_REQUEST", 
                "Completed stock request: " + saved.getRequestNumber() + " by " + processor.getUsername(),
                "STOCK_REQUEST", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public StockRequestResponse reject(Long id, String reason) {
        StockRequest stockRequest = requireRequest(id);
        if (stockRequest.getStatus() != StockRequestStatus.REQUESTED && stockRequest.getStatus() != StockRequestStatus.PROCESSING) {
            throw new BusinessException("Hanya pengajuan REQUESTED atau PROCESSING yang dapat ditolak.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("Alasan penolakan (reason/rejectReason) wajib diisi");
        }

        User processor = currentUserService.requireCurrentUser();
        validateStockRequestPermission(stockRequest, processor);

        stockRequest.setStatus(StockRequestStatus.REJECTED);
        stockRequest.setProcessedBy(processor);
        stockRequest.setProcessedAt(LocalDateTime.now());
        stockRequest.setRejectReason(reason.trim());
        stockRequest.setCompletedAt(LocalDateTime.now());

        StockRequest saved = stockRequestRepository.save(stockRequest);
        activityLogService.record("REJECT_STOCK_REQUEST", 
                "Rejected stock request: " + saved.getRequestNumber() + " by " + processor.getUsername() + ". Reason: " + reason,
                "STOCK_REQUEST", saved.getId());
        return mapToResponse(saved);
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
                stockRequest.getRequestedBy().getUsername(),
                stockRequest.getRequestedByRole(),
                stockRequest.getTargetRole(),
                "STOCK_REQUEST",
                stockRequest.getRequestedAt(),
                stockRequest.getProcessedBy() != null
                        ? stockRequest.getProcessedBy().getFullName()
                        : null,
                stockRequest.getProcessedBy() != null
                        ? stockRequest.getProcessedBy().getUsername()
                        : null,
                stockRequest.getProcessedAt(),
                stockRequest.getCompletedAt(),
                stockRequest.getRejectReason()
        );
    }
}
