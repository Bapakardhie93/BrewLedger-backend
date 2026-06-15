package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.pos.PosCatalogResponse;
import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.service.PosService;
import com.brewledger.brewledger.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('KASIR', 'ADMIN')")
public class PosController {

    private final PosService posService;
    private final TransactionService transactionService;

    @GetMapping("/catalog")
    public PosCatalogResponse getCatalog(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        return posService.getCatalog(keyword);
    }

    @PostMapping("/checkout")
    public TransactionResponse checkout(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        return transactionService.create(request);
    }
}
