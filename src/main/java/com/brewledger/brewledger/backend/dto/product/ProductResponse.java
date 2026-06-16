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

    private Boolean useCustomHpp;
    private Double customHpp;
    private Double calculatedHpp;
    private Double hpp;
    private Double margin;
    private Double recommendedSellingPrice;
    private java.util.List<com.brewledger.brewledger.backend.dto.recipe.ProductRecipeResponse> recipeItems;
}