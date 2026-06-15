package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductResponse {
    private String productName;
    private Long quantitySold;
    private Double revenue;
}
