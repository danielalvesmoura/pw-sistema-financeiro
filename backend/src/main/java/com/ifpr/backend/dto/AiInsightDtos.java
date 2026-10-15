package com.ifpr.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AiInsightDtos {
    private AiInsightDtos() {}

    public record InsightRequest(
        @NotNull(message = "informe a data inicial") LocalDate startDate,
        @NotNull(message = "informe a data final") LocalDate endDate
    ) {}

    public record CategoryExpense(
        String category,
        BigDecimal total,
        BigDecimal percentage
    ) {}

    public record FinancialContext(
        Long walletId,
        String walletName,
        String currency,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netResult,
        int totalTransactions,
        List<CategoryExpense> topExpenseCategories
    ) {}

    public record InsightResponse(
        FinancialContext context,
        String insight,
        String model,
        LocalDateTime generatedAt
    ) {}
}
