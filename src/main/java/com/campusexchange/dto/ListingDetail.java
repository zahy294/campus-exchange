package com.campusexchange.dto;

/** A listing as one viewer sees it on its detail page: the listing plus the {@link SellerCard} for that viewer. */
public final class ListingDetail<T> {

    private final T listing;
    private final SellerCard seller;

    public ListingDetail(T listing, SellerCard seller) {
        this.listing = listing;
        this.seller = seller;
    }

    public T getListing() {
        return listing;
    }

    public SellerCard getSeller() {
        return seller;
    }
}
