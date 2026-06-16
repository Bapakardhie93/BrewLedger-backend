package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.shift.CashierShiftResponse;
import com.brewledger.brewledger.backend.dto.shift.CloseShiftRequest;
import com.brewledger.brewledger.backend.dto.shift.OpenShiftRequest;
import com.brewledger.brewledger.backend.entity.CashierShift;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.enums.ShiftStatus;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.CashierShiftRepository;
import com.brewledger.brewledger.backend.repository.TransactionRepository;
import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashierShiftService {

    private final CashierShiftRepository cashierShiftRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;

    @Transactional
    public CashierShiftResponse openShift(Long targetUserId, OpenShiftRequest request) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User target tidak ditemukan"));

        if (!Boolean.TRUE.equals(targetUser.getActive())) {
            throw new BusinessException("User target tidak aktif");
        }

        String roleName = targetUser.getRole() != null ? targetUser.getRole().getName() : "";
        if (!"KASIR".equals(roleName) && !"GUDANG".equals(roleName)) {
            throw new BusinessException("Role target tidak valid");
        }

        if (cashierShiftRepository.existsByCashierIdAndStatus(targetUser.getId(), ShiftStatus.OPEN)) {
            throw new com.brewledger.brewledger.backend.exception.ConflictException("User target sudah memiliki shift aktif");
        }

        if (request.getOpeningCash() == null || request.getOpeningCash() < 0) {
            throw new BusinessException("Opening cash tidak boleh negatif");
        }

        CashierShift shift = new CashierShift();
        shift.setOpeningCash(request.getOpeningCash());
        shift.setStatus(ShiftStatus.OPEN);
        shift.setOpenedAt(LocalDateTime.now());
        shift.setCashier(targetUser);
        shift.setNotes(request.getNotes());

        cashierShiftRepository.save(shift);
        log.info("Shift opened for target user: {}, opening cash: {}", targetUser.getUsername(), request.getOpeningCash());
        activityLogService.record("SHIFT_OPENED", 
                "Membuka shift untuk " + targetUser.getFullName() + " (username: " + targetUser.getUsername() + ")", 
                "CASHIER_SHIFT", shift.getId());
        return mapToResponse(shift);
    }

    @Transactional
    public CashierShiftResponse closeShift(Long id, CloseShiftRequest request) {
        CashierShift shift = cashierShiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift tidak ditemukan"));

        if (shift.getStatus() == ShiftStatus.CLOSED) {
            throw new BusinessException("Shift ini sudah ditutup");
        }

        if (request.getClosingCash() == null || request.getClosingCash() < 0) {
            throw new BusinessException("Closing cash tidak boleh negatif");
        }

        LocalDateTime closedAt = LocalDateTime.now();
        Double cashSales = transactionRepository.sumCashTransactions(shift.getCashier().getId(), shift.getOpenedAt(), closedAt);
        Double expectedCash = shift.getOpeningCash() + (cashSales != null ? cashSales : 0.0);
        Double cashDifference = request.getClosingCash() - expectedCash;

        shift.setClosingCash(request.getClosingCash());
        shift.setExpectedCash(expectedCash);
        shift.setCashDifference(cashDifference);
        shift.setStatus(ShiftStatus.CLOSED);
        shift.setClosedAt(closedAt);
        if (request.getNotes() != null) {
            shift.setNotes(request.getNotes());
        }

        cashierShiftRepository.save(shift);
        log.info("Shift closed for user: {}, closing cash: {}, expected: {}, diff: {}",
                shift.getCashier().getUsername(), request.getClosingCash(), expectedCash, cashDifference);
        activityLogService.record("SHIFT_CLOSED", 
                "Menutup shift untuk " + shift.getCashier().getFullName() + " (username: " + shift.getCashier().getUsername() + "). Closing Cash: Rp " + request.getClosingCash() + ", Expected Cash: Rp " + expectedCash + ", Difference: Rp " + cashDifference, 
                "CASHIER_SHIFT", shift.getId());

        return mapToResponse(shift);
    }

    @Transactional(readOnly = true)
    public CashierShiftResponse getCurrentShift() {
        User currentUser = currentUserService.requireCurrentUser();
        return cashierShiftRepository.findByCashierIdAndStatus(currentUser.getId(), ShiftStatus.OPEN)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CashierShiftResponse> findAll(String status, String role, String date) {
        List<CashierShift> shifts = cashierShiftRepository.findAll();
        return shifts.stream()
                .filter(shift -> {
                    if (status != null && !status.trim().isEmpty()) {
                        if (!shift.getStatus().name().equalsIgnoreCase(status.trim())) {
                            return false;
                        }
                    }
                    if (role != null && !role.trim().isEmpty()) {
                        if (shift.getCashier().getRole() == null || 
                            !shift.getCashier().getRole().getName().equalsIgnoreCase(role.trim())) {
                            return false;
                        }
                    }
                    if (date != null && !date.trim().isEmpty()) {
                        if (shift.getOpenedAt() == null || 
                            !shift.getOpenedAt().toLocalDate().toString().equals(date.trim())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CashierShiftResponse findById(Long id) {
        CashierShift shift = cashierShiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift tidak ditemukan dengan ID: " + id));
        
        User currentUser = currentUserService.requireCurrentUser();
        String currentRole = currentUser.getRole() != null ? currentUser.getRole().getName() : "";
        if (!"MANAGEMENT".equals(currentRole) && !shift.getCashier().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki izin untuk melihat detail shift ini.");
        }
        
        return mapToResponse(shift);
    }

    public boolean hasActiveShift(Long userId) {
        return cashierShiftRepository.existsByCashierIdAndStatus(userId, ShiftStatus.OPEN);
    }

    private CashierShiftResponse mapToResponse(CashierShift shift) {
        return new CashierShiftResponse(
                shift.getId(),
                shift.getCashier().getId(),
                shift.getCashier().getFullName(),
                shift.getCashier().getUsername(),
                shift.getCashier().getRole() != null ? shift.getCashier().getRole().getName() : "",
                shift.getOpeningCash(),
                shift.getClosingCash(),
                shift.getExpectedCash(),
                shift.getCashDifference(),
                shift.getStatus().name(),
                shift.getOpenedAt(),
                shift.getClosedAt(),
                shift.getNotes()
        );
    }
}
