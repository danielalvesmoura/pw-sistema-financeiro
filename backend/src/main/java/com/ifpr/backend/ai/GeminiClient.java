package com.ifpr.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.ifpr.backend.exception.BusinessException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeminiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
        @Value("${gemini.api-key:}") String apiKey,
        @Value("${gemini.model:gemini-3.8-flash}") String model
    ) {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "gemini-3.8-flash" : model.trim();
    }

    public String generate(String prompt) {
        ensureConfigured();

        try {
            JsonNode candidate = requestCandidate(prompt, 4096);
            if ("MAX_TOKENS".equals(finishReason(candidate))) {
                // Uma única nova tentativa com mais espaço para o raciocínio e a resposta.
                candidate = requestCandidate(prompt, 8192);
            }
            return extractCompleteText(candidate);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                throw new BusinessException("O limite de uso da IA foi atingido. Tente novamente mais tarde.");
            }
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new BusinessException("A chave da API Gemini é inválida ou não tem permissão de acesso.");
            }
            throw new BusinessException("Não foi possível gerar a análise com a IA no momento.");
        } catch (RestClientException ex) {
            throw new BusinessException("Não foi possível conectar ao serviço de IA no momento.");
        }
    }

    public String model() {
        return model;
    }

    private void ensureConfigured() {
        if (apiKey.isBlank()) {
            throw new BusinessException(
                "Integração com IA não configurada. Defina a variável de ambiente GEMINI_API_KEY."
            );
        }
    }

    private JsonNode requestCandidate(String prompt, int maxOutputTokens) {
        Map<String, Object> payload = Map.of(
            "contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            )),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", maxOutputTokens
            )
        );

        JsonNode response = restClient.post()
            .uri("/v1beta/models/{model}:generateContent", model)
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-goog-api-key", apiKey)
            .body(payload)
            .retrieve()
            .body(JsonNode.class);

        return response == null ? null : response.path("candidates").path(0);
    }

    private static String finishReason(JsonNode candidate) {
        return candidate == null ? "" : candidate.path("finishReason").asText("");
    }

    static String extractCompleteText(JsonNode candidate) {
        String reason = finishReason(candidate);
        if ("MAX_TOKENS".equals(reason)) {
            throw new BusinessException("A IA atingiu o limite de geração. Tente novamente para obter uma análise completa.");
        }
        if (!"STOP".equals(reason)) {
            throw new BusinessException("A IA não concluiu a análise. Tente novamente.");
        }

        JsonNode parts = candidate.path("content").path("parts");
        StringBuilder result = new StringBuilder();
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                if (!part.path("thought").asBoolean(false)) {
                    result.append(part.path("text").asText(""));
                }
            }
        }
        String text = result.toString().trim();
        if (text.isBlank()) {
            throw new BusinessException("A IA não retornou uma análise. Tente novamente.");
        }
        return text;
    }
}
