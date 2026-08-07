package com.kartyavya.access.exception;

/**
 * Thrown when an ADMIN attempts to disable their own account via
 * {@code PATCH /api/admin/users/{id}/status}.
 *
 * <p>HTTP mapping: 400 / INVALID_REQUEST / fieldErrors=null.
 * This prevents accidental self-lockout as documented in api-contracts.md §1 business rules.
 */
public class SelfActionNotAllowedException extends RuntimeException {

    public SelfActionNotAllowedException(String message) {
        super(message);
    }
}
