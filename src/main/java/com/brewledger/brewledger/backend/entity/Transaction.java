package com.brewledger.brewledger.backend.entity;

import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
import com.brewledger.brewledger.backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String transactionNumber;

    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Double subtotal;

    private Double tax;

    private Double total;

    private String notes;
}