package com.brewledger.brewledger.backend.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PosProductResponse {

    private Long id;

    private String code;

    private String name;

    private String categoryName;

    private Double sellingPrice;

    private Boolean available;

    private Long maximumOrderQuantity;
}
