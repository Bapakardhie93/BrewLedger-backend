package com.brewledger.brewledger.backend.dto.purchase;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePurchaseOrderRequest {

    @NotNull(message = "Supplier wajib dipilih")
    private Long supplierId;

    private String notes;
}