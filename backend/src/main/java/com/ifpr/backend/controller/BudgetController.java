package com.ifpr.backend.controller;

import com.ifpr.backend.dto.BudgetDtos.*;
import com.ifpr.backend.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets/{walletId}/budget")
@Tag(name = "Orçamento mensal")
public class BudgetController {
    private final BudgetService service;
    public BudgetController(BudgetService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Consulta limite mensal e despesas do mês selecionado")
    public BudgetResponse get(@PathVariable Long walletId, @RequestParam String month) {
        return service.get(walletId, parseMonth(month));
    }

    @PutMapping
    @Operation(summary = "Define ou remove o limite mensal recorrente; somente o dono")
    public BudgetResponse update(@PathVariable Long walletId, @RequestParam String month,
        @Valid @RequestBody BudgetRequest request) {
        return service.update(walletId, request, parseMonth(month));
    }

    private YearMonth parseMonth(String month) {
        try {
            if (!month.matches("[0-9]{4}-[0-9]{2}")) throw new IllegalArgumentException();
            YearMonth result = YearMonth.parse(month);
            if (result.getYear() < 1900 || result.getYear() > 9999) throw new IllegalArgumentException();
            return result;
        } catch (java.time.DateTimeException | IllegalArgumentException ex) {
            throw new com.ifpr.backend.exception.BadRequestException("Informe o mês no formato AAAA-MM.");
        }
    }
}
