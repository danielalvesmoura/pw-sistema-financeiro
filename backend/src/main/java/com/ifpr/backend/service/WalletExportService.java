package com.ifpr.backend.service;

import com.ifpr.backend.exception.BadRequestException;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.Carteira;
import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.model.Transacao;
import com.ifpr.backend.repository.CarteiraRepository;
import com.ifpr.backend.repository.TransacaoRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CarteiraRepository carteiraRepository;
    private final TransacaoRepository transacaoRepository;
    private final WalletAuthorizationService auth;

    public WalletExportService(
        CarteiraRepository carteiraRepository,
        TransacaoRepository transacaoRepository,
        WalletAuthorizationService auth
    ) {
        this.carteiraRepository = carteiraRepository;
        this.transacaoRepository = transacaoRepository;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public ExportedWallet export(Long walletId, String typeValue, String formatValue) {
        auth.requireMember(walletId);
        Carteira wallet = carteiraRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));

        TipoTransacao type = parseType(typeValue);
        ExportFormat format = parseFormat(formatValue);
        List<Transacao> transactions = transacaoRepository.findByCarteiraId(walletId).stream()
                .filter(t -> type == null || t.getTipo() == type)
                .sorted(Comparator.comparing(Transacao::getData).reversed()
                        .thenComparing(Transacao::getId, Comparator.reverseOrder()))
                .toList();

        String suffix = type == null ? "completa" : type == TipoTransacao.INCOME ? "receitas" : "despesas";
        String baseName = sanitize(wallet.getNome()) + "_" + suffix;

        if (format == ExportFormat.TXT) {
            return new ExportedWallet(
                    baseName + ".txt",
                    "text/plain; charset=UTF-8",
                    createTxt(wallet, transactions, type));
        }
        return new ExportedWallet(
                baseName + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                createXlsx(wallet, transactions, type));
    }

    private byte[] createTxt(Carteira wallet, List<Transacao> items, TipoTransacao filter) {
        StringBuilder out = new StringBuilder();
        out.append("Carteira: ").append(wallet.getNome()).append('\n');
        out.append("Conteúdo: ").append(filterLabel(filter)).append('\n');
        out.append("Exportado em: ").append(LocalDate.now().format(DATE_FORMAT)).append("\n\n");
        out.append("Data | Tipo | Descrição | Categoria | Valor | Forma de pagamento | Observações | Criado por\n");
        out.append("--------------------------------------------------------------------------------------------\n");
        for (Transacao t : items) {
            out.append(t.getData().format(DATE_FORMAT)).append(" | ")
                    .append(typeLabel(t.getTipo())).append(" | ")
                    .append(text(t.getDescricao(), "Sem descrição")).append(" | ")
                    .append(categoryLabel(t)).append(" | ")
                    .append(t.getValor().toPlainString()).append(" | ")
                    .append(text(t.getFormaPagamento(), "Não informado")).append(" | ")
                    .append(text(t.getObservacoes(), "")).append(" | ")
                    .append(t.getCriadoPor().getNome()).append('\n');
        }
        out.append("\nTotal de lançamentos: ").append(items.size()).append('\n');
        out.append("Total: ")
                .append(items.stream()
                        .map(Transacao::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .toPlainString())
                .append('\n');
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] createXlsx(Carteira wallet, List<Transacao> items, TipoTransacao filter) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transações");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            int rowIndex = 0;
            Row title = sheet.createRow(rowIndex++);
            title.createCell(0).setCellValue("Carteira");
            title.createCell(1).setCellValue(wallet.getNome());
            Row content = sheet.createRow(rowIndex++);
            content.createCell(0).setCellValue("Conteúdo");
            content.createCell(1).setCellValue(filterLabel(filter));
            Row exported = sheet.createRow(rowIndex++);
            exported.createCell(0).setCellValue("Exportado em");
            Cell exportedDate = exported.createCell(1);
            exportedDate.setCellValue(java.sql.Date.valueOf(LocalDate.now()));
            exportedDate.setCellStyle(dateStyle);
            rowIndex++;

            String[] headers = {
                    "Data",
                    "Tipo",
                    "Descrição",
                    "Categoria",
                    "Valor",
                    "Forma de pagamento",
                    "Observações",
                    "Criado por"
            };
            Row header = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Transacao t : items) {
                Row row = sheet.createRow(rowIndex++);
                Cell date = row.createCell(0);
                date.setCellValue(java.sql.Date.valueOf(t.getData()));
                date.setCellStyle(dateStyle);
                row.createCell(1).setCellValue(typeLabel(t.getTipo()));
                row.createCell(2).setCellValue(text(t.getDescricao(), "Sem descrição"));
                row.createCell(3).setCellValue(categoryLabel(t));
                Cell amount = row.createCell(4);
                amount.setCellValue(t.getValor().doubleValue());
                amount.setCellStyle(moneyStyle);
                row.createCell(5).setCellValue(text(t.getFormaPagamento(), "Não informado"));
                row.createCell(6).setCellValue(text(t.getObservacoes(), ""));
                row.createCell(7).setCellValue(t.getCriadoPor().getNome());
            }

            Row total = sheet.createRow(rowIndex + 1);
            total.createCell(3).setCellValue("Total");
            Cell totalValue = total.createCell(4);
            totalValue.setCellValue(
                    items.stream()
                            .map(Transacao::getValor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .doubleValue());
            totalValue.setCellStyle(moneyStyle);

            int[] widths = {14, 12, 30, 22, 15, 22, 35, 22};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            sheet.createFreezePane(0, 5);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível gerar o arquivo XLSX.", ex);
        }
    }


    private String categoryLabel(Transacao transaction) {
        // A exportação representa as transações da carteira. Por isso, mantém o nome
        // da categoria vinculada à transação, mesmo quando a categoria pertence a
        // outro usuário. Isso não concede acesso ao cadastro da categoria.
        return transaction.getCategoria() == null
                ? "Sem categoria"
                : transaction.getCategoria().getNome();
    }

    private TipoTransacao parseType(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("ALL")) {
            return null;
        }

        try {
            return TipoTransacao.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Tipo de exportação inválido.");
        }
    }

    private ExportFormat parseFormat(String value) {
        if (value == null || value.isBlank()) {
            return ExportFormat.XLSX;
        }

        try {
            return ExportFormat.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Formato de exportação inválido. Use TXT ou XLSX.");
        }
    }

    private String filterLabel(TipoTransacao type) {
        if (type == null) {
            return "Carteira inteira";
        }
        return type == TipoTransacao.INCOME ? "Apenas receitas" : "Apenas despesas";
    }

    private String typeLabel(TipoTransacao type) {
        return type == TipoTransacao.INCOME ? "Receita" : "Despesa";
    }

    private String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.replace('\n', ' ').replace('\r', ' ');
    }

    private String sanitize(String value) {
        String cleaned = value == null
                ? "carteira"
                : value.trim().replaceAll(
                        "[^a-zA-Z0-9áàâãéêíóôõúçÁÀÂÃÉÊÍÓÔÕÚÇ_-]+",
                        "_");
        return cleaned.isBlank() ? "carteira" : cleaned;
    }

    private enum ExportFormat {
        TXT,
        XLSX
    }

    public record ExportedWallet(
            String fileName,
            String contentType,
            byte[] content) {}

}
