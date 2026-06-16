package com.brewledger.brewledger.backend.dto.table;

import com.brewledger.brewledger.backend.enums.TableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTableStatusRequest {

    @NotNull
    private TableStatus status;
}
