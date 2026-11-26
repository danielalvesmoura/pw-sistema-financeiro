package com.ifpr.backend.service;

import static com.ifpr.backend.dto.ReportDtos.*;

import com.ifpr.backend.model.TipoTransacao;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ReportCsvService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] transactions(TransactionReportResponse report) {
        StringBuilder csv = new StringBuilder();

        // BOM ajuda Excel e outros editores a reconhecerem corretamente UTF-8.
        csv.append('\uFEFF');
        row(csv, "Extrato de transações");
        row(csv, "Carteira", safeText(report.walletName()));
        row(csv, "Período", DATE_FORMAT.format(report.startDate()) + " a " + DATE_FORMAT.format(report.endDate()));
        row(csv, "Moeda", safeText(report.currency()));
        row(csv, "Receitas", report.totalIncome().toPlainString());
        row(csv, "Despesas", report.totalExpense().toPlainString());
        row(csv, "Resultado do período", report.netResult().toPlainString());
        row(csv, "Quantidade de transações", String.valueOf(report.totalTransactions()));
        csv.append('\n');

        row(csv, "Data", "Tipo", "Categoria", "Descrição", "Valor", "Forma de pagamento", "Criado por");

        for (TransactionReportItem item : report.transactions()) {
            row(
                csv,
                DATE_FORMAT.format(item.date()),
                typeLabel(item.type()),
                safeText(item.categoryName()),
                safeText(item.description()),
                item.amount().toPlainString(),
                safeText(item.paymentMethod()),
                safeText(item.createdByName())
            );
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String typeLabel(TipoTransacao type) {
        return type == TipoTransacao.INCOME ? "Receita" : "Despesa";
    }

    private void row(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(';');
            }

            csv.append(escape(values[i]));
        }

        csv.append('\n');
    }

    private String safeText(String value) {
        if (value == null || value.isEmpty()) return value;
        String trimmed = value.stripLeading();
        if (value.startsWith("\t") || value.startsWith("\r") || value.startsWith("\n")
            || (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0)) {
            return "'" + value;
        }
        return value;
    }

    private String escape(String value) {
        String normalized = value == null ? "" : value.replace("\"", "\"\"");
        return '"' + normalized + '"';
    }
}
