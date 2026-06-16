package com.brewledger.brewledger.backend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransactionResponse {

    private Long id;

    private String transactionNumber;

    private Double subtotal;

    private Double tax;

    private Double total;

    private Double discountAmount;

    private String discountNotes;

    private String customerName;

    private String tableNumber;

    private Double cashReceived;

    private Double changeAmount;

    private List<TransactionItemResponse> items;

    private java.time.LocalDateTime transactionDate;

    private String status;

    private String transactionType;

    private String paymentMethod;

    private String cashierName;

    private String invoiceNumber;
}