package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.SummaryDtos.SummaryResponse;
import static com.ifpr.backend.dto.TransactionDtos.*;

import com.ifpr.backend.exception.BadRequestException;
import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets/{walletId}")
public class TransactionController {
    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> SORT_FIELDS = Map.of(
        "date", "data",
        "amount", "valor",
        "description", "descricao",
        "type", "tipo",
        "createdAt", "criadoEm"
    );

    private static final Set<String> INTERNAL_SORT_FIELDS = Set.copyOf(SORT_FIELDS.values());

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/transactions")
    public Page<TransactionResponse> list(
        @PathVariable Long walletId,
        @RequestParam(required = false) TipoTransacao type,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Pageable pageable
    ) {
        return service.list(walletId, type, categoryId, startDate, endDate, normalizePageable(pageable));
    }

    private Pageable normalizePageable(Pageable pageable) {
        int pageSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        Sort mappedSort = Sort.by(pageable.getSort().stream().map(order -> {
            String requestedProperty = order.getProperty();
            String property = SORT_FIELDS.getOrDefault(requestedProperty, requestedProperty);

            if (!INTERNAL_SORT_FIELDS.contains(property)) {
                throw new BadRequestException("Campo de ordenação inválido.");
            }

            return new Sort.Order(order.getDirection(), property);
        }).toList());

        return PageRequest.of(pageable.getPageNumber(), pageSize, mappedSort);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
        @PathVariable Long walletId,
        @Valid @RequestBody TransactionRequest request
    ) {
        return service.create(walletId, request);
    }

    @GetMapping("/transactions/{id}")
    public TransactionResponse get(
        @PathVariable Long walletId,
        @PathVariable Long id
    ) {
        return service.get(walletId, id);
    }

    @PutMapping("/transactions/{id}")
    public TransactionResponse update(
        @PathVariable Long walletId,
        @PathVariable Long id,
        @Valid @RequestBody TransactionRequest request
    ) {
        return service.update(walletId, id, request);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long walletId,
        @PathVariable Long id
    ) {
        service.delete(walletId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public SummaryResponse summary(
        @PathVariable Long walletId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return service.summary(walletId, startDate, endDate);
    }
}
