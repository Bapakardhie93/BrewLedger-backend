package com.brewledger.brewledger.backend.dto.transaction;

import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero
    private Double discountAmount;

    private String discountNotes;

    private String customerName;

    private String tableNumber;

    @PositiveOrZero
    private Double cashReceived;

    @NotEmpty
    private List<@NotNull @Valid CreateTransactionItemRequest> items;
}
