package com.brewledger.brewledger.backend.dto.transaction;

import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTransactionRequest {

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private PaymentMethod paymentMethod;

    private String notes;

    private Double discountAmount;

    private String discountNotes;

    private String customerName;

    private String tableNumber;

    private Double cashReceived;

    @NotEmpty
    private List<CreateTransactionItemRequest> items;
}