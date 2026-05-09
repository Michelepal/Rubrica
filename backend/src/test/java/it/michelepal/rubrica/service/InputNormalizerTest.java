package it.michelepal.rubrica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InputNormalizer Tests")
public
class InputNormalizerTest {

    private InputNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new InputNormalizer();
    }

    @Test
    @DisplayName("text: should normalize text by stripping and collapsing spaces")
    void testTextNormalization() {
        String result = normalizer.text("  hello   world  ");
        assertEquals("hello world", result);
    }

    @Test
    @DisplayName("text: should return null for null input")
    void testTextNullInput() {
        String result = normalizer.text(null);
        assertNull(result);
    }

    @Test
    @DisplayName("text: should return null for blank string")
    void testTextBlankString() {
        String result = normalizer.text("   ");
        assertNull(result);
    }

    @Test
    @DisplayName("text: should return null for empty string")
    void testTextEmptyString() {
        String result = normalizer.text("");
        assertNull(result);
    }

    @Test
    @DisplayName("text: should handle multiple consecutive spaces")
    void testTextMultipleSpaces() {
        String result = normalizer.text("test    with    spaces");
        assertEquals("test with spaces", result);
    }

    @Test
    @DisplayName("text: should handle tabs and newlines")
    void testTextTabsAndNewlines() {
        String result = normalizer.text("test\t\n  with\n\ttabs");
        assertEquals("test with tabs", result);
    }

    @Test
    @DisplayName("requiredText: should return normalized text")
    void testRequiredTextWithValue() {
        String result = normalizer.requiredText("  hello  ");
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("requiredText: should return empty string for null input")
    void testRequiredTextNullInput() {
        String result = normalizer.requiredText(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("requiredText: should return empty string for blank input")
    void testRequiredTextBlankInput() {
        String result = normalizer.requiredText("   ");
        assertEquals("", result);
    }

    @Test
    @DisplayName("email: should convert to lowercase")
    void testEmailLowercase() {
        String result = normalizer.email("Test@EXAMPLE.COM");
        assertEquals("test@example.com", result);
    }

    @Test
    @DisplayName("email: should normalize and lowercase")
    void testEmailNormalizeAndLowercase() {
        String result = normalizer.email("  Test@EXAMPLE.COM  ");
        assertEquals("test@example.com", result);
    }

    @Test
    @DisplayName("email: should return null for null input")
    void testEmailNullInput() {
        String result = normalizer.email(null);
        assertNull(result);
    }

    @Test
    @DisplayName("email: should return null for blank input")
    void testEmailBlankInput() {
        String result = normalizer.email("   ");
        assertNull(result);
    }
}
