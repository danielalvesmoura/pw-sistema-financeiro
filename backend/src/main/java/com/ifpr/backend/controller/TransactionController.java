package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.SummaryDtos.SummaryResponse;
import static com.ifpr.backend.dto.TransactionDtos.*;

import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
        Sort mappedSort = Sort.by(pageable.getSort().stream().map(order -> {
            String property = switch (order.getProperty()) {
                case "date" -> "data";
                case "amount" -> "valor";
                case "description" -> "descricao";
                case "type" -> "tipo";
                case "createdAt" -> "criadoEm";
                default -> order.getProperty();
            };
            return new Sort.Order(order.getDirection(), property);
        }).toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@PathVariable Long walletId, @Valid @RequestBody TransactionRequest request) {
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
    public TransactionResponse update(@PathVariable Long walletId, @PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return service.update(walletId, id, request);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long walletId, @PathVariable Long id) {
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
