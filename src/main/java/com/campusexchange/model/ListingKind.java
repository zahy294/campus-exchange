package com.campusexchange.model;

import java.util.Optional;

/** The kinds of {@link Listing} a student can post. Used to address a listing generically. */
public enum ListingKind {
    BOOK("books", "Book", "book listing"),
    DEVICE("devices", "Device", "device listing");

    private final String pathSegment;
    private final String displayName;
    private final String noun;

    ListingKind(String pathSegment, String displayName, String noun) {
        this.pathSegment = pathSegment;
        this.displayName = displayName;
        this.noun = noun;
    }

    /** URL segment for web pages, e.g. "books" in /books/12. */
    public String getPathSegment() {
        return pathSegment;
    }

    /** "Book" / "Device", used in not-found messages. */
    public String getDisplayName() {
        return displayName;
    }

    /** "book listing" / "device listing", used in permission messages. */
    public String getNoun() {
        return noun;
    }

    public static Optional<ListingKind> fromPathSegment(String segment) {
        for (ListingKind kind : values()) {
            if (kind.pathSegment.equals(segment)) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
