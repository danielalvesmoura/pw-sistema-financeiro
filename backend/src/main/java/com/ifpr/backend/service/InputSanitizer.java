package com.ifpr.backend.service;

import com.ifpr.backend.exception.BadRequestException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cc}&&[^\\n\\t]]");
    private static final Pattern HTML_TAG = Pattern.compile("(?is)<\\s*/?\\s*[a-z][^>]*>");
    private static final Set<String> SAFE_URL_SCHEMES = Set.of("http", "https");

    public String requiredText(String value, String fieldName) {
        String cleaned = text(value);

        if (cleaned == null) {
            throw new BadRequestException(fieldName + " não pode ficar vazio.");
        }

        return cleaned;
    }

    public String text(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        String withoutControls = CONTROL_CHARACTERS.matcher(normalized).replaceAll("");
        String withoutHtmlTags = HTML_TAG.matcher(withoutControls).replaceAll("");
        String cleaned = withoutHtmlTags.trim();

        return cleaned.isBlank() ? null : cleaned;
    }

    public String httpUrl(String value, String fieldName) {
        String cleaned = text(value);

        if (cleaned == null) {
            return null;
        }

        try {
            URI uri = new URI(cleaned);
            String scheme = uri.getScheme();

            if (scheme == null || !SAFE_URL_SCHEMES.contains(scheme.toLowerCase())) {
                throw new BadRequestException(fieldName + " deve usar http ou https.");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new BadRequestException(fieldName + " deve conter uma URL válida.");
            }

            return uri.toString();
        } catch (URISyntaxException ex) {
            throw new BadRequestException(fieldName + " deve conter uma URL válida.");
        }
    }
}
