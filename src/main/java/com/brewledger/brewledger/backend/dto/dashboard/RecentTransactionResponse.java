package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransactionResponse {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private String cashierName;
    private LocalDateTime transactionDate;
    private Double total;
    private String status;
    private String transactionType;
    private String paymentMethod;
}
