package com.ifpr.backend.service;

import com.ifpr.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InputSanitizerTest {
    private final InputSanitizer sanitizer = new InputSanitizer();
    @Test void removesTagsAndTrims() {
        assertThat(sanitizer.text("  <b>Mercado</b>  ")).isEqualTo("Mercado");
    }
    @Test void rejectsBlankRequiredText() {
        assertThatThrownBy(() -> sanitizer.requiredText(" <b></b> ", "Nome"))
            .isInstanceOf(BadRequestException.class);
    }
    @Test void rejectsJavascriptUrl() {
        assertThatThrownBy(() -> sanitizer.httpUrl("javascript:alert(1)", "Anexo"))
            .isInstanceOf(BadRequestException.class);
        assertThat(sanitizer.httpUrl("https://example.com/arquivo", "Anexo"))
            .isEqualTo("https://example.com/arquivo");
    }
}
