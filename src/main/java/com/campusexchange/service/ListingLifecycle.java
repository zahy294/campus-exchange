package com.campusexchange.service;

import com.campusexchange.exception.ForbiddenOperationException;
import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.Book;
import com.campusexchange.model.Device;
import com.campusexchange.model.Listing;
import com.campusexchange.model.ListingKind;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.repository.BookRepository;
import com.campusexchange.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * The Listing lifecycle: the only module that decides whether a student may
 * change a listing, and the only place a listing is sold, relisted or withdrawn.
 * Book and Device modules use it for every owner-only operation, and web and API
 * callers go through it, so ownership and status rules live in exactly one place.
 *
 * <p>Errors: unknown id -> {@link ResourceNotFoundException} (404);
 * someone else's listing -> {@link ForbiddenOperationException} (403);
 * missing status -> {@link IllegalArgumentException} (400).
 */
@Service
public class ListingLifecycle {

    private final BookRepository bookRepository;
    private final DeviceRepository deviceRepository;

    public ListingLifecycle(BookRepository bookRepository, DeviceRepository deviceRepository) {
        this.bookRepository = bookRepository;
        this.deviceRepository = deviceRepository;
    }

    /** Loads a listing (seller fetched) for its owner. */
    @Transactional(readOnly = true)
    public Listing requireOwned(ListingKind kind, Long id, String ownerEmail) {
        Listing listing = find(kind, id);
        if (!listing.isOwnedBy(ownerEmail)) {
            throw new ForbiddenOperationException("You do not have permission to modify this " + kind.getNoun() + ".");
        }
        return listing;
    }

    /** Sell ({@code SOLD}) or relist ({@code AVAILABLE}) a listing the caller owns. */
    @Transactional
    public Listing changeStatus(ListingKind kind, Long id, ListingStatus status, String ownerEmail) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required: AVAILABLE or SOLD");
        }
        Listing listing = requireOwned(kind, id, ownerEmail);
        listing.setStatus(status);
        return listing;
    }

    /** Permanently removes a listing the caller owns. */
    @Transactional
    public void withdraw(ListingKind kind, Long id, String ownerEmail) {
        Listing listing = requireOwned(kind, id, ownerEmail);
        switch (kind) {
            case BOOK -> bookRepository.delete((Book) listing);
            case DEVICE -> deviceRepository.delete((Device) listing);
        }
    }

    private Listing find(ListingKind kind, Long id) {
        Optional<? extends Listing> found = switch (kind) {
            case BOOK -> bookRepository.findByIdWithSeller(id);
            case DEVICE -> deviceRepository.findByIdWithSeller(id);
        };
        return found.orElseThrow(() ->
                new ResourceNotFoundException(kind.getDisplayName() + " not found with id: " + id));
    }
}
