package com.brewledger.brewledger.backend.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSupplierRequest {

    @NotBlank(message = "Nama supplier wajib diisi")
    private String name;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;
}