package com.ifpr.backend.service;

import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.model.Transacao;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FinancialTotalsTest {
    @Test void calculatesExactDecimalTotals() {
        var totals = FinancialTotals.from(List.of(tx(TipoTransacao.INCOME, "100.10"),
            tx(TipoTransacao.EXPENSE, "30.20"), tx(TipoTransacao.EXPENSE, "0.10")));
        assertThat(totals.income()).isEqualByComparingTo("100.10");
        assertThat(totals.expense()).isEqualByComparingTo("30.30");
        assertThat(totals.net()).isEqualByComparingTo("69.80");
    }
    @Test void emptyListHasZeroTotals() {
        assertThat(FinancialTotals.from(List.of()).net()).isZero();
    }
    private Transacao tx(TipoTransacao type, String value) {
        var tx = new Transacao();
        tx.setTipo(type); tx.setValor(new BigDecimal(value));
        return tx;
    }
}
