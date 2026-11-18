package com.campusexchange.exception;

/** Raised when registering with an email that already has an account. */
public class DuplicateEmailException extends IllegalArgumentException {
    public DuplicateEmailException(String email) {
        super("An account with email " + email + " already exists.");
    }
}
