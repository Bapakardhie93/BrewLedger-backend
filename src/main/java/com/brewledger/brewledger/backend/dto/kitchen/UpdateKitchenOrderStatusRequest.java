package com.brewledger.brewledger.backend.dto.kitchen;

import com.brewledger.brewledger.backend.enums.KitchenOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateKitchenOrderStatusRequest {

    @NotNull
    private KitchenOrderStatus status;
}
