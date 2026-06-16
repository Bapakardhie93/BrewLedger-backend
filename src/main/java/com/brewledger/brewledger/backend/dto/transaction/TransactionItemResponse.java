package com.brewledger.brewledger.backend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionItemResponse {

    private Long productId;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double subtotal;

    private Double costPrice;

    private Double subtotalCost;

    private String notes;
}