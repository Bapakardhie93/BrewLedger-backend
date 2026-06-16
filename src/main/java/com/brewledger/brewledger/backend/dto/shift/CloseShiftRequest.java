package com.brewledger.brewledger.backend.dto.shift;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseShiftRequest {

    @NotNull
    private Double closingCash;

    private String notes;
}
