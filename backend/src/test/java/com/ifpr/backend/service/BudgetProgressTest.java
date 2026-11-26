package com.ifpr.backend.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetProgressTest {
    @Test void calculatesRemainingAndPercentage() {
        var progress = BudgetProgress.calculate(new BigDecimal("1000"), new BigDecimal("250"));
        assertThat(progress.remaining()).isEqualByComparingTo("750");
        assertThat(progress.percentage()).isEqualByComparingTo("25.0");
    }
    @Test void handlesOverBudget() {
        var progress = BudgetProgress.calculate(new BigDecimal("100"), new BigDecimal("125"));
        assertThat(progress.remaining()).isEqualByComparingTo("-25");
        assertThat(progress.percentage()).isEqualByComparingTo("125.0");
    }
    @Test void noLimitMeansNoPercentage() {
        assertThat(BudgetProgress.calculate(null, BigDecimal.TEN).percentage()).isNull();
    }
}
