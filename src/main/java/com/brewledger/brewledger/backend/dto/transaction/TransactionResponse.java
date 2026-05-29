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

    private List<TransactionItemResponse> items;
}