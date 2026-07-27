package com.kartyavya.access.exception;

// Thrown when a registration attempt uses an email already in the users table.
// HTTP mapping: 409 DUPLICATE_RESOURCE (member-ownership.md). fieldErrors=null in response.
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
