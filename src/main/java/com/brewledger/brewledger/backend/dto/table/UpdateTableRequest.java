package com.brewledger.brewledger.backend.dto.table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTableRequest {

    @NotBlank
    private String number;

    @NotNull
    @Min(1)
    private Integer capacity;

    private String location;
}
