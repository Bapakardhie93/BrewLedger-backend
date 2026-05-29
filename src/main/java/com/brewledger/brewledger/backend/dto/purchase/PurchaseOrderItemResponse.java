package com.brewledger.brewledger.backend.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseOrderItemResponse {

    private Long id;

    private String ingredientName;

    private Double quantity;

    private Double unitPrice;

    private Double subtotal;
}