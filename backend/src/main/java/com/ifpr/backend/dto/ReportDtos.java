package com.ifpr.backend.dto;

import com.ifpr.backend.model.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ReportDtos {
    private ReportDtos() {}

    public record TransactionReportItem(
        Long id,
        LocalDate date,
        TipoTransacao type,
        String categoryName,
        String description,
        BigDecimal amount,
        String paymentMethod,
        String createdByName
    ) {}

    public record TransactionReportResponse(
        Long walletId,
        String walletName,
        String currency,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netResult,
        int totalTransactions,
        List<TransactionReportItem> transactions
    ) {}
}
