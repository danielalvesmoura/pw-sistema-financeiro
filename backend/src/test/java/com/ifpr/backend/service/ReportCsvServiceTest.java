package com.ifpr.backend.service;

import com.ifpr.backend.dto.ReportDtos.*;
import com.ifpr.backend.model.TipoTransacao;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReportCsvServiceTest {
    @Test void exportsUtf8AndPreventsTextFormulaWithoutChangingNumericTotals() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        var item = new TransactionReportItem(1L, date, TipoTransacao.EXPENSE, "Mercado", "=1+1",
            BigDecimal.TEN, "Pix", "Pessoa");
        var report = new TransactionReportResponse(1L, "Casa", "BRL", date, date,
            BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.TEN.negate(), 1, List.of(item));
        String csv = new String(new ReportCsvService().transactions(report), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("\uFEFF").contains("\"'=1+1\"", "\"-10\"", "01/01/2026");
    }
}
