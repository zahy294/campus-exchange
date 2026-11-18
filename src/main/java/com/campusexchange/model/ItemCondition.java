package com.campusexchange.model;

/**
 * How worn a listed item is. Stored by name. This enum is the only source of the
 * display label, the plain-language hint and the wear grade (4 = unused, 1 = clear wear),
 * for server-rendered pages and, via the API, for the browser-rendered device catalogue.
 */
public enum ItemCondition {
    NEW("New", "Unused, as bought", 4),
    LIKE_NEW("Like new", "Used briefly, no visible wear", 3),
    GOOD("Good", "Normal use, light marks", 2),
    FAIR("Fair", "Clear wear, fully usable", 1);

    private final String label;
    private final String hint;
    private final int grade;

    ItemCondition(String label, String hint, int grade) {
        this.label = label;
        this.hint = hint;
        this.grade = grade;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }

    /** Bars filled in the wear meter: 4 for NEW down to 1 for FAIR. */
    public int getGrade() {
        return grade;
    }
}
