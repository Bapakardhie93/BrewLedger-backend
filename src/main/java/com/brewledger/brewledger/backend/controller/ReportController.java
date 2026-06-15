package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.report.InventoryReportResponse;
import com.brewledger.brewledger.backend.dto.report.PurchaseReportResponse;
import com.brewledger.brewledger.backend.dto.report.SalesReportResponse;
import com.brewledger.brewledger.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public SalesReportResponse getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return reportService.getSalesReport(startDate, endDate);
    }

    @GetMapping("/purchases")
    public PurchaseReportResponse getPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return reportService.getPurchaseReport(startDate, endDate);
    }

    @GetMapping("/inventory")
    public InventoryReportResponse getInventoryReport() {
        return reportService.getInventoryReport();
    }
}
