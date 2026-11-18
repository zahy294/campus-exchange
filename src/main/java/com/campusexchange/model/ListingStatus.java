package com.campusexchange.model;

public enum ListingStatus {
    AVAILABLE("Available"),
    SOLD("Sold");

    private final String label;

    ListingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
