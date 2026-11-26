package com.ifpr.backend.service;

import com.ifpr.backend.dto.BudgetDtos.*;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.Carteira;
import com.ifpr.backend.model.PapelCarteira;
import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.repository.CarteiraRepository;
import com.ifpr.backend.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {
    private final CarteiraRepository wallets;
    private final TransacaoRepository transactions;
    private final WalletAuthorizationService auth;

    public BudgetService(CarteiraRepository wallets, TransacaoRepository transactions, WalletAuthorizationService auth) {
        this.wallets = wallets;
        this.transactions = transactions;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public BudgetResponse get(Long walletId, YearMonth month) {
        var member = auth.requireMember(walletId);
        return response(wallet(walletId), month, member.getPapel() == PapelCarteira.OWNER);
    }

    @Transactional
    public BudgetResponse update(Long walletId, BudgetRequest request, YearMonth month) {
        auth.requireOwner(walletId);
        Carteira wallet = wallet(walletId);
        wallet.setLimiteMensal(request.limit());
        return response(wallet, month, true);
    }

    private Carteira wallet(Long walletId) {
        return wallets.findById(walletId).orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
    }

    private BudgetResponse response(Carteira wallet, YearMonth month, boolean canEdit) {
        BigDecimal spent = transactions.sumByWalletAndTypeAndPeriod(wallet.getId(), TipoTransacao.EXPENSE,
            month.atDay(1), month.atEndOfMonth());
        if (spent == null) spent = BigDecimal.ZERO;
        var progress = BudgetProgress.calculate(wallet.getLimiteMensal(), spent);
        return new BudgetResponse(wallet.getId(), wallet.getMoeda(), month.toString(), wallet.getLimiteMensal(),
            spent, progress.remaining(), progress.percentage(), canEdit);
    }
}
