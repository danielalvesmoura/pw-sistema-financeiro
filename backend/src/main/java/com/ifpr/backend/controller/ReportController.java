package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.ReportDtos.TransactionReportResponse;

import com.ifpr.backend.service.ReportCsvService;
import com.ifpr.backend.service.ReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets/{walletId}/reports")
public class ReportController {
    private final ReportService reportService;
    private final ReportCsvService csvService;

    public ReportController(ReportService reportService, ReportCsvService csvService) {
        this.reportService = reportService;
        this.csvService = csvService;
    }

    @GetMapping("/transactions")
    public TransactionReportResponse transactions(
        @PathVariable Long walletId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.transactions(walletId, startDate, endDate);
    }

    @GetMapping(value = "/transactions.csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadTransactions(
        @PathVariable Long walletId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TransactionReportResponse report = reportService.transactions(walletId, startDate, endDate);
        byte[] content = csvService.transactions(report);
        String filename = "extrato-carteira-" + walletId + "-" + startDate + "-a-" + endDate + ".csv";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(filename).build().toString()
            )
            .body(content);
    }
}
