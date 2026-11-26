package com.ifpr.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public final class BudgetDtos {
    private BudgetDtos() {}

    // null remove o limite; a carteira continua existindo.
    public record BudgetRequest(@DecimalMin("0.01") @Digits(integer = 13, fraction = 2) BigDecimal limit) {}
    public record BudgetResponse(Long walletId, String currency, String month,
        BigDecimal limit, BigDecimal spent, BigDecimal remaining, BigDecimal percentage,
        boolean canEdit) {}
}
