package com.ifpr.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public final class SummaryDtos {
    private SummaryDtos() {}

    public record CategoryTotal(
        Long categoryId, 
        String categoryName, 
        BigDecimal total
    ) {}

    public record MonthTotal(
        String month, 
        BigDecimal income, 
        BigDecimal expense
    ) {}

    public record SummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        long transactionCount,
        List<CategoryTotal> byCategory,
        List<MonthTotal> byMonth
    ) {}
}
