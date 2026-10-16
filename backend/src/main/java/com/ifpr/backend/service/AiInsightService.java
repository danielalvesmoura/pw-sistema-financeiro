package com.ifpr.backend.service;

import static com.ifpr.backend.dto.AiInsightDtos.*;

import com.ifpr.backend.ai.GeminiClient;
import com.ifpr.backend.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AiInsightService {
    private final AiFinancialContextService contextService;
    private final GeminiClient geminiClient;

    public AiInsightService(
        AiFinancialContextService contextService,
        GeminiClient geminiClient
    ) {
        this.contextService = contextService;
        this.geminiClient = geminiClient;
    }

    public InsightResponse generate(Long walletId, InsightRequest request) {
        FinancialContext context = contextService.build(
            walletId,
            request.startDate(),
            request.endDate()
        );

        if (context.totalTransactions() == 0) {
            throw new BusinessException(
                "Não há transações no período selecionado para gerar uma análise."
            );
        }

        String insight = geminiClient.generate(buildPrompt(context));
        return new InsightResponse(context, insight, geminiClient.model(), LocalDateTime.now());
    }

    private String buildPrompt(FinancialContext context) {
        String categories = context.topExpenseCategories().isEmpty()
            ? "Nenhuma despesa categorizada."
            : context.topExpenseCategories().stream()
                .map(item -> String.format(
                    "- %s: %s %s (%s%% das despesas)",
                    item.category(),
                    item.total(),
                    context.currency(),
                    item.percentage()
                ))
                .collect(Collectors.joining("\n"));

        return """
            Você é um assistente de educação financeira dentro de um sistema de gestão financeira pessoal e compartilhada.
            Analise exclusivamente os dados fornecidos abaixo. Não invente informações, não presuma renda futura e não recomende investimentos ou produtos financeiros.
            Escreva em português do Brasil, de forma objetiva e útil.

            Dados reais da carteira:
            - Carteira: %s
            - Período: %s até %s
            - Moeda: %s
            - Receitas: %s
            - Despesas: %s
            - Resultado do período: %s
            - Quantidade de transações: %d
            - Maiores categorias de despesa:
            %s

            Gere uma análise curta com exatamente estas três seções:
            1. Diagnóstico
            2. Pontos de atenção
            3. Ações práticas

            Em "Ações práticas", sugira no máximo três ações simples baseadas somente nos dados apresentados.
            """.formatted(
                context.walletName(),
                context.startDate(),
                context.endDate(),
                context.currency(),
                context.totalIncome(),
                context.totalExpense(),
                context.netResult(),
                context.totalTransactions(),
                categories
            );
    }
}
