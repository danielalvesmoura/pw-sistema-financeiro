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

        Map<String, Object> payload = Map.of(
            "contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            )),
            "generationConfig", Map.of(
                "temperature", 0.3,
                "maxOutputTokens", 700
            )
        );

        try {
            JsonNode response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-goog-api-key", apiKey)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

            String text = extractText(response);
            if (text.isBlank()) {
                throw new BusinessException("A IA não retornou uma análise. Tente novamente.");
            }

            return text;
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

    private String extractText(JsonNode response) {
        if (response == null) {
            return "";
        }

        JsonNode parts = response.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (JsonNode part : parts) {
            String text = part.path("text").asText("").trim();
            if (!text.isBlank()) {
                if (!result.isEmpty()) {
                    result.append("\n");
                }
                result.append(text);
            }
        }
        return result.toString().trim();
    }
}
