package com.ifpr.backend.service;

import static com.ifpr.backend.dto.ReportDtos.*;

import com.ifpr.backend.exception.BadRequestException;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.Carteira;
import com.ifpr.backend.model.Categoria;
import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.model.Transacao;
import com.ifpr.backend.repository.CarteiraRepository;
import com.ifpr.backend.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final long MAX_PERIOD_DAYS = 366;

    private final TransacaoRepository transacaoRepository;
    private final CarteiraRepository carteiraRepository;
    private final WalletAuthorizationService auth;
    private final CurrentUserService currentUserService;

    public ReportService(
        TransacaoRepository transacaoRepository,
        CarteiraRepository carteiraRepository,
        WalletAuthorizationService auth,
        CurrentUserService currentUserService
    ) {
        this.transacaoRepository = transacaoRepository;
        this.carteiraRepository = carteiraRepository;
        this.auth = auth;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public TransactionReportResponse transactions(Long walletId, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        auth.requireMember(walletId);

        Carteira wallet = carteiraRepository.findById(walletId)
            .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));

        Long currentUserId = currentUserService.get().getId();
        List<Transacao> transactions = transacaoRepository
            .findByCarteiraIdAndDataBetweenOrderByDataAscIdAsc(walletId, startDate, endDate);

        BigDecimal income = transactions.stream()
            .filter(transaction -> transaction.getTipo() == TipoTransacao.INCOME)
            .map(Transacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
            .filter(transaction -> transaction.getTipo() == TipoTransacao.EXPENSE)
            .map(Transacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionReportItem> items = transactions.stream()
            .map(transaction -> toReportItem(transaction, currentUserId))
            .toList();

        return new TransactionReportResponse(
            wallet.getId(),
            wallet.getNome(),
            wallet.getMoeda(),
            startDate,
            endDate,
            income,
            expense,
            income.subtract(expense),
            items.size(),
            items
        );
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Informe a data inicial e a data final do relatório.");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("A data inicial não pode ser posterior à data final.");
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) >= MAX_PERIOD_DAYS) {
            throw new BadRequestException("O período do relatório deve ter no máximo 366 dias.");
        }
    }

    private TransactionReportItem toReportItem(Transacao transaction, Long currentUserId) {
        Categoria category = transaction.getCategoria();
        boolean ownCategory = category != null
            && category.getUsuario() != null
            && category.getUsuario().getId().equals(currentUserId);

        return new TransactionReportItem(
            transaction.getId(),
            transaction.getData(),
            transaction.getTipo(),
            ownCategory ? category.getNome() : "Sem categoria",
            transaction.getDescricao(),
            transaction.getValor(),
            transaction.getFormaPagamento(),
            transaction.getCriadoPor().getNome()
        );
    }
}
