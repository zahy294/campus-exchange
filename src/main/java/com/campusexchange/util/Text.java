package com.campusexchange.util;

/** Small string helpers used when copying user input into entities. */
public final class Text {

    private Text() {
    }

    /** Trims the value; returns null for null or blank input. */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Canonical form of an email address used for storage and lookups. */
    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
