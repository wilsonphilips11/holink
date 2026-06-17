package com.holink.validation.util;

import java.util.regex.Pattern;

public final class SanitizationUtil {

    private static final Pattern HTML_PATTERN = Pattern.compile(
            "<[^>]*>|javascript:|on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private SanitizationUtil() {
    }

    public static boolean isFreeOfHtml(String value) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        if (SCRIPT_PATTERN.matcher(trimmed).find()) {
            return false;
        }
        return !HTML_PATTERN.matcher(trimmed).find();
    }

    public static String sanitizeText(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
