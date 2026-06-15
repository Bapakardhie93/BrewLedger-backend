package com.brewledger.brewledger.backend.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PurchaseApprovalResponse {

    private Long purchaseOrderId;

    private String poNumber;

    private String supplierName;

    private LocalDate orderDate;

    private String status;

    private String requestedBy;

    private LocalDateTime submittedAt;

    private Long itemCount;

    private Double totalAmount;

    private String notes;

    private String rejectionReason;
}
