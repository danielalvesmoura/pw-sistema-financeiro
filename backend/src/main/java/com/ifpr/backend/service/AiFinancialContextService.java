package com.ifpr.backend.service;

import static com.ifpr.backend.dto.AiInsightDtos.*;

import com.ifpr.backend.dto.ReportDtos.TransactionReportItem;
import com.ifpr.backend.dto.ReportDtos.TransactionReportResponse;
import com.ifpr.backend.model.TipoTransacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiFinancialContextService {
    private final ReportService reportService;

    public AiFinancialContextService(ReportService reportService) {
        this.reportService = reportService;
    }

    public FinancialContext build(Long walletId, LocalDate startDate, LocalDate endDate) {
        TransactionReportResponse report = reportService.transactions(walletId, startDate, endDate);

        Map<String, BigDecimal> expensesByCategory = new LinkedHashMap<>();
        for (TransactionReportItem item : report.transactions()) {
            if (item.type() != TipoTransacao.EXPENSE) {
                continue;
            }

            String category = item.categoryName() == null || item.categoryName().isBlank()
                ? "Sem categoria"
                : item.categoryName();

            expensesByCategory.merge(category, item.amount(), BigDecimal::add);
        }

        List<CategoryExpense> topCategories = expensesByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .map(entry -> new CategoryExpense(
                entry.getKey(),
                entry.getValue(),
                percentage(entry.getValue(), report.totalExpense())
            ))
            .toList();

        return new FinancialContext(
            report.walletId(),
            report.walletName(),
            report.currency(),
            report.startDate(),
            report.endDate(),
            report.totalIncome(),
            report.totalExpense(),
            report.netResult(),
            report.totalTransactions(),
            topCategories
        );
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return value
            .multiply(BigDecimal.valueOf(100))
            .divide(total, 1, RoundingMode.HALF_UP);
    }
}
