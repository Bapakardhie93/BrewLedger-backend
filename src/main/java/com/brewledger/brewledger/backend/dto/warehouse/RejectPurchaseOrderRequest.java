package com.brewledger.brewledger.backend.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPurchaseOrderRequest {

    @NotBlank
    private String reason;
}
