package com.brewledger.brewledger.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transaction_items")
public class TransactionItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double subtotal;

    private Double costPrice = 0.0;

    private Double subtotalCost = 0.0;

    private String notes;
}