package com.brewledger.brewledger.backend.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseOrderDetailResponse {

    private Long id;

    private String poNumber;

    private String supplierName;

    private LocalDate orderDate;

    private String status;

    private String notes;

    private List<PurchaseOrderItemResponse> items;
}
