package com.brewledger.brewledger.backend.dto.shift;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenShiftRequest {

    private Long userId;

    @NotNull
    private Double openingCash;

    private String notes;
}
