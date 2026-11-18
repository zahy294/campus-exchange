package com.campusexchange.service;

import org.springframework.data.domain.Sort;

import java.util.Locale;

/**
 * The sort orders offered for browsing listings. Forms render their options from
 * this enum and {@link Paging} turns the chosen key into a query sort, so the
 * page, the API and the query can't drift apart.
 */
public enum ListingSort {
    NEWEST("newest", "Newest first", Sort.by("createdAt").descending()),
    PRICE_ASC("price_asc", "Price: low to high", Sort.by("price").ascending().and(Sort.by("createdAt").descending())),
    PRICE_DESC("price_desc", "Price: high to low", Sort.by("price").descending().and(Sort.by("createdAt").descending())),
    OLDEST("oldest", "Oldest first", Sort.by("createdAt").ascending());

    private final String key;
    private final String label;
    private final Sort sort;

    ListingSort(String key, String label, Sort sort) {
        this.key = key;
        this.label = label;
        this.sort = sort;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public Sort getSort() {
        return sort;
    }

    /** Unknown or missing keys fall back to NEWEST; legacy "desc"/"asc" map to newest/oldest. */
    public static ListingSort fromKey(String key) {
        String k = key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
        if ("asc".equals(k)) {
            return OLDEST;
        }
        for (ListingSort s : values()) {
            if (s.key.equals(k)) {
                return s;
            }
        }
        return NEWEST;
    }
}
