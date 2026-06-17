package com.holink.validation;

import com.holink.validation.util.UrlValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com",
            "http://example.com/path",
            "https://google.com/search?q=test"
    })
    void shouldAllowSafeUrls(String url) {
        assertTrue(UrlValidationUtil.isSafeHttpUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "data:text/html,<script>alert(1)</script>",
            "vbscript:msgbox",
            "file:///etc/passwd",
            "google.com",
            "ftp://example.com",
            "https://",
            "https:///path",
            "https://exa mple.com",
            "https://user:pass@example.com"
    })
    void shouldRejectUnsafeUrls(String url) {
        assertFalse(UrlValidationUtil.isSafeHttpUrl(url));
    }

    @Test
    void shouldRejectBlankUrl() {
        assertFalse(UrlValidationUtil.isSafeHttpUrl(""));
        assertFalse(UrlValidationUtil.isSafeHttpUrl(null));
    }
}
