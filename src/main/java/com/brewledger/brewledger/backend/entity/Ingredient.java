package com.brewledger.brewledger.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ingredients")
public class Ingredient extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Double currentStock = 0.0;

    @Column(nullable = false)
    private Double minimumStock = 0.0;

    @Column(nullable = false)
    private Double costPrice = 0.0;

    @Column(nullable = false)
    private Boolean active = true;
}