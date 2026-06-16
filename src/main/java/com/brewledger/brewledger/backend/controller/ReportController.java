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
@PreAuthorize("hasAnyRole('MANAGEMENT')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public SalesReportResponse getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "groupBy", required = false, defaultValue = "DAY") String groupBy
    ) {
        LocalDate start = from != null ? from : startDate;
        LocalDate end = to != null ? to : endDate;
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        return reportService.getSalesReport(start, end, groupBy);
    }

    @GetMapping("/purchases")
    public PurchaseReportResponse getPurchaseReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "groupBy", required = false, defaultValue = "DAY") String groupBy
    ) {
        LocalDate start = from != null ? from : startDate;
        LocalDate end = to != null ? to : endDate;
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        return reportService.getPurchaseReport(start, end, groupBy);
    }

    @GetMapping("/inventory")
    public InventoryReportResponse getInventoryReport() {
        return reportService.getInventoryReport();
    }

    @GetMapping("/sales/csv")
    public org.springframework.http.ResponseEntity<String> exportSalesReportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "groupBy", required = false, defaultValue = "DAY") String groupBy
    ) {
        LocalDate start = from != null ? from : startDate;
        LocalDate end = to != null ? to : endDate;
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        String csv = reportService.exportSalesReportCsv(start, end, groupBy);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales_report_" + start + "_to_" + end + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(csv);
    }

    @GetMapping("/purchases/csv")
    public org.springframework.http.ResponseEntity<String> exportPurchaseReportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "groupBy", required = false, defaultValue = "DAY") String groupBy
    ) {
        LocalDate start = from != null ? from : startDate;
        LocalDate end = to != null ? to : endDate;
        if (start == null) {
            start = LocalDate.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        String csv = reportService.exportPurchaseReportCsv(start, end, groupBy);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase_report_" + start + "_to_" + end + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(csv);
    }

    @GetMapping("/inventory/csv")
    public org.springframework.http.ResponseEntity<String> exportInventoryReportCsv() {
        String csv = reportService.exportInventoryReportCsv();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_report_" + LocalDate.now() + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(csv);
    }
}
