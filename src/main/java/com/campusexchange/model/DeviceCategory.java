package com.campusexchange.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * The fixed set of device categories. Forms, filters, API labels and validation
 * ({@link com.campusexchange.dto.validation.DeviceCategoryName}) all read this enum,
 * so adding a category is a one-line change.
 */
public enum DeviceCategory {
    LAPTOP("Laptop"),
    PHONE("Phone"),
    TABLET("Tablet"),
    CALCULATOR("Calculator"),
    ACCESSORY("Accessory"),
    OTHER("Other");

    private final String label;

    DeviceCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** True if the value names a category, ignoring case and surrounding spaces. */
    public static boolean isKnown(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).anyMatch(c -> c.name().equals(v));
    }

    /** "LAPTOP, PHONE, ..." for error messages. */
    public static String allNames() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    /** Human label for a stored category string, falling back to the raw value. */
    public static String labelOf(String category) {
        if (category == null) {
            return "";
        }
        for (DeviceCategory c : values()) {
            if (c.name().equalsIgnoreCase(category.trim())) {
                return c.label;
            }
        }
        return category;
    }
}
