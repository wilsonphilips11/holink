package com.holink.validation.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UrlValidationUtil {

    private static final Set<String> BLOCKED_SCHEMES = Set.of(
            "javascript", "data", "vbscript", "file"
    );

    private static final Pattern ALLOWED_SCHEME = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNSAFE_CHARS = Pattern.compile("[\\p{Cntrl}\\s]");

    private UrlValidationUtil() {
    }

    public static boolean isSafeHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String trimmed = url.trim();

        if (UNSAFE_CHARS.matcher(trimmed).find()) {
            return false;
        }

        if (!ALLOWED_SCHEME.matcher(trimmed).find()) {
            return false;
        }

        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (BLOCKED_SCHEMES.contains(normalizedScheme)) {
                return false;
            }
            return ("http".equals(normalizedScheme) || "https".equals(normalizedScheme))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getUserInfo() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
