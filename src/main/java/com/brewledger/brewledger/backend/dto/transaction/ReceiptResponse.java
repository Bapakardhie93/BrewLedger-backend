package com.brewledger.brewledger.backend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReceiptResponse {
    private String storeName;
    private String transactionNumber;
    private LocalDateTime transactionDate;
    private String cashierName;
    private String customerName;
    private String tableNumber;
    private String paymentMethod;
    private Double subtotal;
    private Double tax;
    private Double discountAmount;
    private Double total;
    private Double cashReceived;
    private Double changeAmount;
    private List<TransactionItemResponse> items;
    private String formattedReceipt;
}
