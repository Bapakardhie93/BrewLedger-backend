package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesResponse {

    private String categoryName;

    private Long quantitySold;

    private Double revenue;

    private Double percentage;
}
