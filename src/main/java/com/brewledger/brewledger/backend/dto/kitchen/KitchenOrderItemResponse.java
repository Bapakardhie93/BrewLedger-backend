package com.brewledger.brewledger.backend.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KitchenOrderItemResponse {
    private Long id;
    private String productName;
    private Integer quantity;
    private String notes;
}
