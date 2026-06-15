package com.brewledger.brewledger.backend.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSupplierRequest {

    @NotBlank(message = "Nama supplier wajib diisi")
    private String name;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    @NotNull(message = "Status aktif wajib diisi")
    private Boolean active;
}
