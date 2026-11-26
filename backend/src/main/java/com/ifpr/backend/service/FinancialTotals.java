package com.ifpr.backend.service;

import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.model.Transacao;
import java.math.BigDecimal;
import java.util.List;

public record FinancialTotals(BigDecimal income, BigDecimal expense) {
    public static FinancialTotals from(List<Transacao> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transacao transaction : transactions) {
            if (transaction.getTipo() == TipoTransacao.INCOME) {
                income = income.add(transaction.getValor());
            } else if (transaction.getTipo() == TipoTransacao.EXPENSE) {
                expense = expense.add(transaction.getValor());
            }
        }
        return new FinancialTotals(income, expense);
    }

    public BigDecimal net() {
        return income.subtract(expense);
    }
}
