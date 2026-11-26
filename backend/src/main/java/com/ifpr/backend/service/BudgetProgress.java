package com.ifpr.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BudgetProgress(BigDecimal remaining, BigDecimal percentage) {
    public static BudgetProgress calculate(BigDecimal limit, BigDecimal spent) {
        if (limit == null) return new BudgetProgress(null, null);
        return new BudgetProgress(limit.subtract(spent), spent.multiply(BigDecimal.valueOf(100))
            .divide(limit, 1, RoundingMode.HALF_UP));
    }
}
