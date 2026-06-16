package com.brewledger.brewledger.backend.dto.table;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableResponse {
    private Long id;
    private String number;
    private Integer capacity;
    private String location;
    private String status;
}
