package com.campusexchange.service;

import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.model.Book;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything one seller has listed, with the dashboard totals:
 * listings still on the board, listings sold, and the asking-price total of what sold.
 * Pure: build it from the seller's books and devices and test it without a database.
 */
public final class MyListingsSummary {

    private final List<Book> books;
    private final List<DeviceResponse> devices;
    private final long activeCount;
    private final long soldCount;
    private final BigDecimal soldValue;

    private MyListingsSummary(List<Book> books, List<DeviceResponse> devices,
                              long activeCount, long soldCount, BigDecimal soldValue) {
        this.books = books;
        this.devices = devices;
        this.activeCount = activeCount;
        this.soldCount = soldCount;
        this.soldValue = soldValue;
    }

    public static MyListingsSummary of(List<Book> books, List<DeviceResponse> devices) {
        long active = books.stream().filter(Book::isAvailable).count()
                + devices.stream().filter(DeviceResponse::isAvailable).count();
        long sold = books.size() + devices.size() - active;
        BigDecimal value = books.stream().filter(b -> !b.isAvailable()).map(Book::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(devices.stream().filter(d -> !d.isAvailable()).map(DeviceResponse::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        return new MyListingsSummary(List.copyOf(books), List.copyOf(devices), active, sold, value);
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<DeviceResponse> getDevices() {
        return devices;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public long getSoldCount() {
        return soldCount;
    }

    public BigDecimal getSoldValue() {
        return soldValue;
    }

    public boolean isEmpty() {
        return books.isEmpty() && devices.isEmpty();
    }
}
