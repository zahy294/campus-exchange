package com.campusexchange.dto;

/**
 * Regular expressions shared by form and API DTOs, so the browser-side
 * {@code pattern} attributes and server-side constraints say the same thing.
 */
public final class ValidationRules {

    private ValidationRules() {
    }

    /** Letters (any script), spaces, dots, apostrophes and hyphens. */
    public static final String PERSON_NAME = "^[\\p{L}][\\p{L} .'-]*$";

    /** Optional Indian mobile number: 10 digits starting 6-9, optional +91 prefix. */
    public static final String PHONE = "^$|^(\\+91[- ]?)?[6-9][0-9]{9}$";

    /** Optional ISBN-10 or ISBN-13, hyphens or spaces allowed between digits. */
    public static final String ISBN = "^$|^(?:[0-9][- ]?){9}[0-9Xx]$|^(?:[0-9][- ]?){12}[0-9]$";

    /** 8 to 64 characters with at least one letter and one digit. */
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,64}$";

    public static final String PASSWORD_MESSAGE = "Use 8 to 64 characters, with at least one letter and one number";

    public static final String MAX_PRICE = "500000.00";
}
