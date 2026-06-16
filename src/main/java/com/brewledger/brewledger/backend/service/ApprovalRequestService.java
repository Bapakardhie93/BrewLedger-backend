package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.approval.ApprovalResponse;
import com.brewledger.brewledger.backend.dto.approval.RejectApprovalRequest;
import com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest;
import com.brewledger.brewledger.backend.entity.ApprovalRequest;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.enums.ApprovalStatus;
import com.brewledger.brewledger.backend.enums.ApprovalType;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.ApprovalRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    private WarehouseService warehouseService;
    private TransactionService transactionService;
    private IngredientService ingredientService;
    private com.brewledger.brewledger.backend.repository.IngredientRepository ingredientRepository;

    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    public void setIngredientService(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Lazy
    @org.springframework.beans.factory.annotation.Autowired
    public void setIngredientRepository(com.brewledger.brewledger.backend.repository.IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public ApprovalResponse submitStockAdjustment(Long ingredientId, StockAdjustmentRequest request) {
        User requester = currentUserService.requireCurrentUser();
        ApprovalRequest approval = new ApprovalRequest();
        approval.setRequestNumber("APR-STK-" + System.currentTimeMillis());
        approval.setType(ApprovalType.STOCK_ADJUSTMENT);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setRequestedBy(requester);
        approval.setRequestedByRole(requester.getRole().getName());
        approval.setTargetRole("MANAGEMENT");
        approval.setReferenceId(ingredientId);
        approval.setReason(request.getReason());

        try {
            approval.setPayloadJson(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new BusinessException("Gagal melakukan serialisasi payload stock adjustment");
        }

        approvalRequestRepository.save(approval);
        log.info("Stock adjustment approval request submitted: {}", approval.getRequestNumber());
        notificationService.sendAlert("Permintaan persetujuan penyesuaian stok baru diajukan oleh " + requester.getUsername());
        return mapToResponse(approval);
    }

    @Transactional
    public ApprovalResponse submitNewIngredient(com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest request) {
        User requester = currentUserService.requireCurrentUser();
        
        if (ingredientRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Kode ingredient sudah digunakan: " + request.getCode());
        }

        ApprovalRequest approval = new ApprovalRequest();
        approval.setRequestNumber("APR-ING-" + System.currentTimeMillis());
        approval.setType(ApprovalType.NEW_INGREDIENT);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setRequestedBy(requester);
        approval.setRequestedByRole(requester.getRole().getName());
        approval.setTargetRole("GUDANG");
        approval.setReason("Pengajuan bahan baku baru: " + request.getName());

        try {
            approval.setPayloadJson(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new BusinessException("Gagal melakukan serialisasi payload new ingredient");
        }

        approvalRequestRepository.save(approval);
        log.info("New ingredient approval request submitted: {}", approval.getRequestNumber());
        notificationService.sendAlert("Permintaan penambahan bahan baku baru '" + request.getName() + 
                "' diajukan oleh " + requester.getUsername() + ". Membutuhkan persetujuan GUDANG.");
        return mapToResponse(approval);
    }

    @Transactional
    public ApprovalResponse submitVoidTransaction(Long transactionId, String reason) {
        User requester = currentUserService.requireCurrentUser();
        ApprovalRequest approval = new ApprovalRequest();
        approval.setRequestNumber("APR-VOID-" + System.currentTimeMillis());
        approval.setType(ApprovalType.VOID_TRANSACTION);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setRequestedBy(requester);
        approval.setRequestedByRole(requester.getRole().getName());
        approval.setTargetRole("MANAGEMENT");
        approval.setReferenceId(transactionId);
        approval.setReason(reason);

        approvalRequestRepository.save(approval);
        log.info("Void transaction approval request submitted: {}", approval.getRequestNumber());
        notificationService.sendAlert("Permintaan persetujuan void transaksi baru diajukan oleh " + requester.getUsername());
        return mapToResponse(approval);
    }

    private void validateApprovalPermission(ApprovalRequest approval, User approver) {
        User requester = approval.getRequestedBy();

        // 1. User yang membuat pengajuan tidak boleh approve/reject pengajuannya sendiri
        if (requester != null && requester.getId().equals(approver.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
        }

        // 2. Jika pengajuan dibuat oleh MANAGEMENT: target role harus GUDANG
        if (requester != null && requester.getRole() != null && requester.getRole().getName().equalsIgnoreCase("MANAGEMENT")) {
            if (!approver.getRole().getName().equalsIgnoreCase("GUDANG")) {
                throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
            }
        }
        // 3. Jika pengajuan dibuat oleh GUDANG: target role harus MANAGEMENT
        else if (requester != null && requester.getRole() != null && requester.getRole().getName().equalsIgnoreCase("GUDANG")) {
            if (!approver.getRole().getName().equalsIgnoreCase("MANAGEMENT")) {
                throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
            }
        }
        // 4. Aturan default fallback berdasarkan tipe pengajuan (misal dibuat oleh KASIR)
        else {
            if (approval.getType() == ApprovalType.NEW_INGREDIENT) {
                if (!approver.getRole().getName().equalsIgnoreCase("GUDANG")) {
                    throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
                }
            } else {
                if (!approver.getRole().getName().equalsIgnoreCase("MANAGEMENT")) {
                    throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk memproses pengajuan ini.");
                }
            }
        }
    }

    @Transactional
    public ApprovalResponse approveRequest(Long id) {
        User approver = currentUserService.requireCurrentUser();
        ApprovalRequest approval = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan approval tidak ditemukan dengan ID: " + id));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException("Hanya pengajuan PENDING yang dapat disetujui.");
        }

        validateApprovalPermission(approval, approver);

        if (approval.getType() == ApprovalType.NEW_INGREDIENT) {
            try {
                com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest payload = 
                        objectMapper.readValue(approval.getPayloadJson(), com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest.class);
                com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse newIng = ingredientService.create(payload);
                approval.setReferenceId(newIng.getId());
            } catch (Exception e) {
                log.error("Gagal mengeksekusi pembuatan ingredient pasca approval", e);
                throw new BusinessException("Gagal memproses pembuatan ingredient: " + e.getMessage());
            }
        } else {
            if (approval.getType() == ApprovalType.STOCK_ADJUSTMENT) {
                try {
                    StockAdjustmentRequest payload = objectMapper.readValue(approval.getPayloadJson(), StockAdjustmentRequest.class);
                    warehouseService.executeStockAdjustmentDirectly(approval.getReferenceId(), payload, approval.getRequestedBy());
                } catch (Exception e) {
                    log.error("Gagal mengeksekusi stock adjustment pasca approval", e);
                    throw new BusinessException("Gagal memproses eksekusi stock adjustment: " + e.getMessage());
                }
            } else if (approval.getType() == ApprovalType.VOID_TRANSACTION) {
                transactionService.executeVoidDirectly(approval.getReferenceId(), approval.getRequestedBy());
            }
        }

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setApprovedBy(approver);
        approvalRequestRepository.save(approval);

        activityLogService.record("APPROVE_REQUEST", "Approved request: " + approval.getRequestNumber() + " by " + approver.getUsername());
        log.info("Approval request approved: {}", approval.getRequestNumber());
        return mapToResponse(approval);
    }

    @Transactional
    public ApprovalResponse rejectRequest(Long id, RejectApprovalRequest request) {
        User approver = currentUserService.requireCurrentUser();
        ApprovalRequest approval = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan approval tidak ditemukan dengan ID: " + id));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException("Hanya pengajuan PENDING yang dapat ditolak.");
        }

        validateApprovalPermission(approval, approver);

        String effectiveReason = request.getEffectiveReason();
        if (effectiveReason.isEmpty()) {
            throw new BusinessException("Alasan penolakan (reason/rejectReason) wajib diisi");
        }

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setApprovedBy(approver);
        approval.setRejectReason(effectiveReason);
        approvalRequestRepository.save(approval);

        activityLogService.record("REJECT_REQUEST", "Rejected request: " + approval.getRequestNumber() + " by " + approver.getUsername() + ". Reason: " + effectiveReason);
        log.info("Approval request rejected: {}", approval.getRequestNumber());
        return mapToResponse(approval);
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> findAll(String targetRole) {
        List<ApprovalRequest> list = approvalRequestRepository.findAll();
        if (targetRole != null && !targetRole.trim().isEmpty()) {
            return list.stream()
                    .filter(a -> a.getTargetRole() != null && a.getTargetRole().equalsIgnoreCase(targetRole.trim()))
                    .map(this::mapToResponse)
                    .toList();
        }
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> findAll() {
        return findAll(null);
    }

    @Transactional(readOnly = true)
    public ApprovalResponse findById(Long id) {
        ApprovalRequest approval = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan approval tidak ditemukan dengan ID: " + id));
        return mapToResponse(approval);
    }

    private ApprovalResponse mapToResponse(ApprovalRequest approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getRequestNumber(),
                approval.getType().name(),
                approval.getStatus().name(),
                approval.getRequestedBy().getUsername(),
                approval.getApprovedBy() != null ? approval.getApprovedBy().getUsername() : null,
                approval.getReason(),
                approval.getRejectReason(),
                approval.getReferenceId(),
                approval.getPayloadJson(),
                approval.getCreatedAt(),
                approval.getRequestedBy().getFullName(),
                approval.getCreatedAt(),
                approval.getRequestedByRole(),
                approval.getTargetRole()
        );
    }
}
