package com.brewledger.brewledger.backend.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class KitchenOrderResponse {
    private Long id;
    private Long transactionId;
    private String transactionNumber;
    private String tableNumber;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private List<KitchenOrderItemResponse> items;
}
