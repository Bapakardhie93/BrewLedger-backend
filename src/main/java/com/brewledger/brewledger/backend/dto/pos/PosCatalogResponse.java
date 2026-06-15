package com.brewledger.brewledger.backend.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PosCatalogResponse {

    private String cashierName;

    private Double taxRate;

    private List<String> paymentMethods;

    private List<PosProductResponse> products;
}
