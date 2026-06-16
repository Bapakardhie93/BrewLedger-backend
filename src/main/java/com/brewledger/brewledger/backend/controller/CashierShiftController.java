package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.shift.CashierShiftResponse;
import com.brewledger.brewledger.backend.dto.shift.CloseShiftRequest;
import com.brewledger.brewledger.backend.dto.shift.OpenShiftRequest;
import com.brewledger.brewledger.backend.service.CashierShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashier-shifts")
@RequiredArgsConstructor
public class CashierShiftController {

    private final CashierShiftService cashierShiftService;

    @PostMapping("/open")
    @PreAuthorize("hasRole('MANAGEMENT')")
    public CashierShiftResponse openShift(
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody OpenShiftRequest request
    ) {
        Long bodyUserId = request.getUserId();
        Long resolvedUserId;

        if (userId != null && bodyUserId != null) {
            if (!userId.equals(bodyUserId)) {
                throw new IllegalArgumentException("userId query dan body tidak sama");
            }
            resolvedUserId = userId;
        } else if (userId != null) {
            resolvedUserId = userId;
        } else if (bodyUserId != null) {
            resolvedUserId = bodyUserId;
        } else {
            throw new IllegalArgumentException("userId wajib diisi");
        }

        return cashierShiftService.openShift(resolvedUserId, request);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('MANAGEMENT')")
    public CashierShiftResponse closeShift(
            @PathVariable Long id,
            @Valid @RequestBody CloseShiftRequest request
    ) {
        return cashierShiftService.closeShift(id, request);
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('KASIR', 'GUDANG', 'MANAGEMENT')")
    public ResponseEntity<CashierShiftResponse> getCurrentShift() {
        CashierShiftResponse shift = cashierShiftService.getCurrentShift();
        if (shift == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(shift);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGEMENT')")
    public List<CashierShiftResponse> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String date
    ) {
        return cashierShiftService.findAll(status, role, date);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR', 'GUDANG')")
    public CashierShiftResponse findById(@PathVariable Long id) {
        return cashierShiftService.findById(id);
    }
}
