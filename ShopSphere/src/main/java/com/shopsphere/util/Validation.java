package com.shopsphere.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Server side input validation. Every value that reaches the database or is
 * rendered into a page is validated here, regardless of what the browser did.
 */
public final class Validation {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");
    private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final Pattern ALPHANUMERIC = Pattern.compile("^[A-Za-z0-9._-]{3,60}$");

    private Validation() {
    }

    public static boolean isEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches() && value.trim().length() <= 190;
    }

    public static boolean isPhone(String value) {
        return value != null && !value.isBlank() && PHONE.matcher(value.trim()).matches();
    }

    public static boolean isPasswordStrong(String value) {
        if (value == null || value.length() < 8 || value.length() > 100) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : value.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    public static boolean isSlug(String value) {
        return value != null && SLUG.matcher(value).matches();
    }

    public static boolean isSku(String value) {
        return value != null && ALPHANUMERIC.matcher(value.trim()).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isLength(String value, int max) {
        return value != null && value.length() <= max;
    }

    public static String trimTo(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /** Escapes a value for safe inclusion in HTML text or attribute context. */
    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Converts arbitrary text into a URL friendly slug. */
    public static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "item";
        }
        String slug = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "item" : slug;
    }

    /** Parses a positive integer, returning the fallback when the value is invalid. */
    public static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Parses a long identifier, returning -1 when the value is not a valid id. */
    public static long parseLong(String value, long fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
