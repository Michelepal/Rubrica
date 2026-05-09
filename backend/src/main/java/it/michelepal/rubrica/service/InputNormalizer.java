package it.michelepal.rubrica.service;

import org.springframework.stereotype.Component;

@Component
public class InputNormalizer {

    public String text(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    public String requiredText(String value) {
        String normalized = text(value);
        return normalized == null ? "" : normalized;
    }

    public String email(String value) {
        String normalized = text(value);
        return normalized == null ? null : normalized.toLowerCase();
    }
}

