package com.brewledger.brewledger.backend.entity;

import com.brewledger.brewledger.backend.enums.KitchenOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "kitchen_orders")
public class KitchenOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    private String tableNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KitchenOrderStatus status = KitchenOrderStatus.WAITING;

    private String notes;

    @OneToMany(mappedBy = "kitchenOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitchenOrderItem> items = new ArrayList<>();
}
