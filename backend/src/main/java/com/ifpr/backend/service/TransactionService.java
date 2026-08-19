package com.ifpr.backend.service;

import static com.ifpr.backend.dto.SummaryDtos.*;
import static com.ifpr.backend.dto.TransactionDtos.*;

import com.ifpr.backend.exception.BusinessException;
import com.ifpr.backend.exception.ResourceNotFoundException;
import com.ifpr.backend.model.*;
import com.ifpr.backend.repository.*;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private final TransacaoRepository transacaoRepository;
    private final CarteiraRepository carteiraRepository;
    private final CategoriaRepository categoriaRepository;
    private final CurrentUserService currentUserService;
    private final WalletAuthorizationService auth;

    public TransactionService(
        TransacaoRepository transacaoRepository,
        CarteiraRepository carteiraRepository,
        CategoriaRepository categoriaRepository,
        CurrentUserService currentUserService,
        WalletAuthorizationService auth
    ) {
        this.transacaoRepository = transacaoRepository;
        this.carteiraRepository = carteiraRepository;
        this.categoriaRepository = categoriaRepository;
        this.currentUserService = currentUserService;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(Long walletId, TipoTransacao type, Long categoryId,
                                          LocalDate startDate, LocalDate endDate, Pageable pageable) {
        auth.requireMember(walletId);
        Long currentUserId = currentUserService.get().getId();
        if (categoryId != null) {
            categoriaRepository.findByIdAndCarteiraIdAndUsuarioId(categoryId, walletId, currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
        }

        Specification<Transacao> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("carteira").get("id"), walletId));
            if (type != null) predicates.add(cb.equal(root.get("tipo"), type));
            if (categoryId != null) predicates.add(cb.equal(root.get("categoria").get("id"), categoryId));
            if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("data"), startDate));
            if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("data"), endDate));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return transacaoRepository.findAll(spec, pageable)
                .map(t -> toResponse(t, currentUserId));
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long walletId, Long id) {
        auth.requireMember(walletId);
        Long currentUserId = currentUserService.get().getId();
        return toResponse(findInWallet(walletId, id), currentUserId);
    }

    @Transactional
    public TransactionResponse create(Long walletId, TransactionRequest request) {
        auth.requireEditor(walletId);
        Carteira wallet = carteiraRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        Usuario currentUser = currentUserService.get();
        Transacao transaction = new Transacao();
        transaction.setCarteira(wallet);
        transaction.setCriadoPor(currentUser);
        apply(transaction, request, currentUser.getId());
        return toResponse(transacaoRepository.save(transaction), currentUser.getId());
    }

    @Transactional
    public TransactionResponse update(Long walletId, Long id, TransactionRequest request) {
        auth.requireEditor(walletId);
        Transacao transaction = findInWallet(walletId, id);
        Long currentUserId = currentUserService.get().getId();
        apply(transaction, request, currentUserId);
        return toResponse(transacaoRepository.save(transaction), currentUserId);
    }

    @Transactional
    public void delete(Long walletId, Long id) {
        auth.requireEditor(walletId);
        transacaoRepository.delete(findInWallet(walletId, id));
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(Long walletId, LocalDate startDate, LocalDate endDate) {
        auth.requireMember(walletId);
        Carteira wallet = carteiraRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        Long currentUserId = currentUserService.get().getId();

        List<Transacao> items = transacaoRepository.findByCarteiraId(walletId).stream()
                .filter(t -> startDate == null || !t.getData().isBefore(startDate))
                .filter(t -> endDate == null || !t.getData().isAfter(endDate))
                .toList();

        BigDecimal income = items.stream().filter(t -> t.getTipo() == TipoTransacao.INCOME)
                .map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = items.stream().filter(t -> t.getTipo() == TipoTransacao.EXPENSE)
                .map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, CategoryTotalAccumulator> categories = new LinkedHashMap<>();
        Map<YearMonth, MonthAccumulator> months = new TreeMap<>();

        for (Transacao t : items) {
            Categoria visibleCategory = isCategoryOwnedBy(t.getCategoria(), currentUserId)
                    ? t.getCategoria()
                    : null;
            String catKey = visibleCategory == null ? "null" : visibleCategory.getId().toString();
            CategoryTotalAccumulator cat = categories.computeIfAbsent(catKey, key -> new CategoryTotalAccumulator(
                    visibleCategory == null ? null : visibleCategory.getId(),
                    visibleCategory == null ? "Sem categoria" : visibleCategory.getNome()));
            cat.total = cat.total.add(t.getValor());

            YearMonth ym = YearMonth.from(t.getData());
            MonthAccumulator month = months.computeIfAbsent(ym, ignored -> new MonthAccumulator());
            if (t.getTipo() == TipoTransacao.INCOME) month.income = month.income.add(t.getValor());
            else month.expense = month.expense.add(t.getValor());
        }

        List<CategoryTotal> byCategory = categories.values().stream()
                .map(c -> new CategoryTotal(c.id, c.name, c.total)).toList();
        List<MonthTotal> byMonth = months.entrySet().stream()
                .map(e -> new MonthTotal(e.getKey().toString(), e.getValue().income, e.getValue().expense)).toList();

        BigDecimal balance = wallet.getSaldoInicial().add(income).subtract(expense);
        return new SummaryResponse(income, expense, balance, items.size(), byCategory, byMonth);
    }

    private void apply(Transacao transaction, TransactionRequest request, Long currentUserId) {
        transaction.setTipo(request.type());
        transaction.setValor(request.amount());
        transaction.setDescricao(blankToNull(request.description()));
        transaction.setData(request.date());
        transaction.setAnexoUrl(blankToNull(request.attachmentUrl()));
        transaction.setObservacoes(blankToNull(request.notes()));
        transaction.setRecorrente(Boolean.TRUE.equals(request.recurring()));
        transaction.setFormaPagamento(blankToNull(request.paymentMethod()));

        if (request.categoryId() == null) {
            // Uma categoria de outro usuário pode estar ligada a uma transação compartilhada.
            // Ela fica invisível e não pode ser removida indiretamente por quem não é seu dono.
            if (transaction.getCategoria() == null
                    || isCategoryOwnedBy(transaction.getCategoria(), currentUserId)) {
                transaction.setCategoria(null);
            }
        } else {
            Categoria category = categoriaRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
            if (!isCategoryOwnedBy(category, currentUserId)) {
                throw new ResourceNotFoundException("Categoria não encontrada.");
            }

            Long walletId = transaction.getCarteira().getId();
            boolean sameWallet = category.getCarteira() != null && category.getCarteira().getId().equals(walletId);
            boolean ownLegacyCategory = category.getCarteira() == null;
            if (!sameWallet && !ownLegacyCategory) {
                throw new ResourceNotFoundException("Categoria não encontrada.");
            }
            if (!category.isAtivo()) {
                throw new BusinessException("A categoria selecionada está inativa.");
            }
            if (category.getTipo() != request.type()) {
                throw new BusinessException("O tipo da categoria deve ser igual ao tipo da transação.");
            }
            transaction.setCategoria(category);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Transacao findInWallet(Long walletId, Long id) {
        Transacao t = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada."));
        if (!t.getCarteira().getId().equals(walletId)) {
            throw new ResourceNotFoundException("Transação não encontrada.");
        }
        return t;
    }

    private TransactionResponse toResponse(Transacao t, Long currentUserId) {
        Categoria category = t.getCategoria();
        boolean ownCategory = isCategoryOwnedBy(category, currentUserId);

        // O nome da categoria faz parte da informação da transação compartilhada e
        // continua visível para os membros da carteira. O ID só é exposto ao dono,
        // evitando que outro usuário reutilize a categoria em novas transações.
        return new TransactionResponse(t.getId(), t.getCarteira().getId(),
                ownCategory ? category.getId() : null,
                category == null ? null : category.getNome(),
                t.getCriadoPor().getId(), t.getCriadoPor().getNome(), t.getTipo(), t.getValor(),
                t.getDescricao(), t.getData(), t.getAnexoUrl(), t.getObservacoes(), t.isRecorrente(),
                t.getFormaPagamento(), t.getCriadoEm(), t.getAtualizadoEm());
    }


    private boolean isCategoryOwnedBy(Categoria category, Long userId) {
        return category != null
                && category.getUsuario() != null
                && category.getUsuario().getId().equals(userId);
    }

    private static class CategoryTotalAccumulator {
        final Long id; final String name; BigDecimal total = BigDecimal.ZERO;
        CategoryTotalAccumulator(Long id, String name) { this.id = id; this.name = name; }
    }
    private static class MonthAccumulator {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
    }
}
