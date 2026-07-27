package com.kartyavya.access.exception;

// Thrown on login when email is not found or BCrypt verification fails.
// HTTP mapping: 401 INVALID_CREDENTIALS (member-ownership.md). fieldErrors=null in response.
// NOTE: same exception for both "not found" and "wrong password" — intentional security practice
// to prevent email enumeration.
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
