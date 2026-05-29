package com.brewledger.brewledger.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank(message = "Nama kategori wajib diisi")
    private String name;

    private String description;
}