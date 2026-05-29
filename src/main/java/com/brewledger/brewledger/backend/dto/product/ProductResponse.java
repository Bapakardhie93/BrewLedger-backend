package com.brewledger.brewledger.backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private String categoryName;
    private Double sellingPrice;
    private String description;
    private Boolean active;
}