package com.brewledger.brewledger.backend.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    private String name;
    private String description;
    private Boolean active;
}
