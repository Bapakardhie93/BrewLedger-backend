package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.service.TransactionService;
import com.brewledger.brewledger.backend.dto.transaction.ReceiptResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('KASIR', 'MANAGEMENT')")
    public TransactionResponse create(
            @Valid
            @RequestBody CreateTransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return service.create(request, idempotencyKey);
    }

    public TransactionResponse create(CreateTransactionRequest request) {
        return create(request, null);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public List<TransactionResponse> findAll() {

        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public TransactionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public void voidTransaction(@PathVariable Long id) {
        service.voidTransaction(id);
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('KASIR', 'MANAGEMENT')")
    public ReceiptResponse getReceipt(@PathVariable Long id) {
        return service.getReceipt(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('KASIR', 'MANAGEMENT')")
    public List<TransactionResponse> findMyTransactions() {
        return service.findMyTransactions();
    }
}
