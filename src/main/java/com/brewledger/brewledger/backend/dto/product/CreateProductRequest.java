package com.brewledger.brewledger.backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "Kode produk wajib diisi")
    private String code;

    @NotBlank(message = "Nama produk wajib diisi")
    private String name;

    @NotNull(message = "Kategori wajib dipilih")
    private Long categoryId;

    @NotNull(message = "Harga wajib diisi")
    @Positive(message = "Harga harus lebih dari 0")
    private Double sellingPrice;

    private String description;
}