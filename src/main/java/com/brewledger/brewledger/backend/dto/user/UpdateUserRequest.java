package com.brewledger.brewledger.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String fullName;

    @NotBlank(message = "Username wajib diisi")
    private String username;

    private String password; // Optional: diisi jika ingin update password

    @NotNull(message = "Role ID wajib diisi")
    private Long roleId;

    private String phoneNumber;
}
