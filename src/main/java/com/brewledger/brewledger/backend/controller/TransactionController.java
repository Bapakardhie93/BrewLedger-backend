package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.service.TransactionService;
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
    @PreAuthorize("hasAnyRole('KASIR', 'ADMIN')")
    public TransactionResponse create(
            @Valid
            @RequestBody CreateTransactionRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public List<TransactionResponse> findAll() {

        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
    public TransactionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }
}
