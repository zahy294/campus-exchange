package com.campusexchange.dto;

import com.campusexchange.model.User;

/**
 * What one viewer may see of a seller. This is the single home of the contact-privacy rule:
 * <ul>
 *   <li>anonymous visitors see the seller's name and department only;</li>
 *   <li>any signed-in student also sees email and phone;</li>
 *   <li>{@link #isViewerIsOwner()} tells views to show owner actions instead of contact details.</li>
 * </ul>
 * The REST API and the web pages both render from this object, so they can't disagree.
 */
public final class SellerCard {

    private final Long id;
    private final String fullName;
    private final String department;
    private final String email;
    private final String phone;
    private final boolean contactVisible;
    private final boolean viewerIsOwner;

    private SellerCard(Long id, String fullName, String department, String email, String phone,
                       boolean contactVisible, boolean viewerIsOwner) {
        this.id = id;
        this.fullName = fullName;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.contactVisible = contactVisible;
        this.viewerIsOwner = viewerIsOwner;
    }

    /**
     * @param seller      the listing's seller (must be initialised)
     * @param viewerEmail the signed-in viewer's email, or null for an anonymous visitor
     */
    public static SellerCard forViewer(User seller, String viewerEmail) {
        boolean signedIn = viewerEmail != null && !viewerEmail.isBlank();
        boolean owner = signedIn && seller.getEmail() != null && seller.getEmail().equalsIgnoreCase(viewerEmail.trim());
        return new SellerCard(
                seller.getId(),
                seller.getFullName(),
                seller.getDepartment(),
                signedIn ? seller.getEmail() : null,
                signedIn ? seller.getPhone() : null,
                signedIn,
                owner);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    /** Null unless {@link #isContactVisible()}. */
    public String getEmail() {
        return email;
    }

    /** Null unless {@link #isContactVisible()} (and the seller gave a phone number). */
    public String getPhone() {
        return phone;
    }

    public boolean isContactVisible() {
        return contactVisible;
    }

    public boolean isViewerIsOwner() {
        return viewerIsOwner;
    }
}
