package com.ifpr.backend.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifpr.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

class GeminiClientTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void returnsCompleteTextWithoutThoughts() throws Exception {
        var candidate = mapper.readTree("""
            {"finishReason":"STOP","content":{"parts":[
                {"thought":true,"text":"Raciocínio interno"},
                {"text":"Diagnóstico\\n"},
                {"text":"Resultado positivo."}
            ]}}
            """);
        assertEquals("Diagnóstico\nResultado positivo.", GeminiClient.extractCompleteText(candidate));
    }

    @Test
    void rejectsTruncatedText() throws Exception {
        var candidate = mapper.readTree("""
            {"finishReason":"MAX_TOKENS","content":{"parts":[{"text":"Resultado incom"}]}}
            """);
        assertThrows(BusinessException.class, () -> GeminiClient.extractCompleteText(candidate));
    }

    @Test
    void rejectsBlockedOrEmptyResponses() throws Exception {
        assertThrows(BusinessException.class, () -> GeminiClient.extractCompleteText(mapper.readTree("{\"finishReason\":\"SAFETY\"}")));
        assertThrows(BusinessException.class, () -> GeminiClient.extractCompleteText(mapper.readTree("{\"finishReason\":\"STOP\"}")));
    }
}
