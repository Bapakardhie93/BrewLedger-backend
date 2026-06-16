package com.brewledger.brewledger.backend.entity;

import com.brewledger.brewledger.backend.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cashier_shifts")
public class CashierShift extends BaseEntity {

    @Column(nullable = false)
    private Double openingCash;

    private Double closingCash;

    private Double expectedCash;

    private Double cashDifference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    private String notes;
}
