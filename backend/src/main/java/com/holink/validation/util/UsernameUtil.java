package com.holink.validation.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class UsernameUtil {

    private static final Pattern VALID_USERNAME = Pattern.compile("^[a-z0-9]+$");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]");

    private UsernameUtil() {
    }

    /**
     * Normalizes raw username input: lowercase, trim, remove special characters and spaces.
     * Example: "John Doe" => "johndoe"
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String lower = raw.trim().toLowerCase(Locale.ROOT);
        return NON_ALPHANUMERIC.matcher(lower).replaceAll("");
    }

    public static boolean isValidNormalized(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        if (username.length() > 30) {
            return false;
        }
        return VALID_USERNAME.matcher(username).matches();
    }

    public static void validateRawInput(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        String trimmed = raw.trim();
        if (trimmed.contains("@")) {
            throw new IllegalArgumentException("Username cannot contain special characters");
        }
        if (isAllUppercaseLetters(trimmed)) {
            throw new IllegalArgumentException("Username must be lowercase");
        }
        if (trimmed.contains(" ") && trimmed.equals(trimmed.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Username cannot contain spaces");
        }
    }

    private static boolean isAllUppercaseLetters(String value) {
        boolean hasLetter = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
                if (Character.isLowerCase(c)) {
                    return false;
                }
            }
        }
        return hasLetter;
    }
}
