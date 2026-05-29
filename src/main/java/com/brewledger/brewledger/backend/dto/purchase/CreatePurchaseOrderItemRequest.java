package com.brewledger.brewledger.backend.dto.purchase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePurchaseOrderItemRequest {

    @NotNull
    private Long ingredientId;

    @NotNull
    @Positive
    private Double quantity;

    @NotNull
    @Positive
    private Double unitPrice;
}