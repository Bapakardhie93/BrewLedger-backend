package com.brewledger.brewledger.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyDashboardMetricResponse {

    private LocalDate date;

    private String dayLabel;

    private Double sales;

    private Long transactionCount;
}
