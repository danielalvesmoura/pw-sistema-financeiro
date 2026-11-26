package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.AiInsightDtos.*;

import com.ifpr.backend.service.AiInsightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Análise com IA")
@RestController
@RequestMapping("/api/v1/wallets/{walletId}/ai")
public class AiInsightController {
    private final AiInsightService aiInsightService;

    public AiInsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Gera análise financeira com Gemini")
    @PostMapping("/insights")
    @ResponseStatus(HttpStatus.OK)
    public InsightResponse generateInsight(
        @PathVariable Long walletId,
        @Valid @RequestBody InsightRequest request
    ) {
        return aiInsightService.generate(walletId, request);
    }
}
