package com.brewledger.brewledger.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "stock_movements")
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double stockBefore;

    @Column(nullable = false)
    private Double stockAfter;

    @Column(nullable = false)
    private String movementType;

    private String referenceNumber;

    private LocalDateTime movementDate;

    private String createdBy;
}