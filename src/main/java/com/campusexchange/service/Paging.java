package com.campusexchange.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Turns raw page/size/sort request parameters into a safe {@link Pageable}.
 * Out-of-range values are clamped instead of failing, so a hand-edited URL never
 * produces an error page. Sort keys are defined by {@link ListingSort}.
 */
public final class Paging {

    public static final int MAX_PAGE_SIZE = 50;

    private Paging() {
    }

    public static Pageable of(int page, int size, String sortKey) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, ListingSort.fromKey(sortKey).getSort());
    }
}
