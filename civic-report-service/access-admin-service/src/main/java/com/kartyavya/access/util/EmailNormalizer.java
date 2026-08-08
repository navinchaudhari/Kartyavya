package com.kartyavya.access.util;

import java.util.Locale;

/**
 * Utility for email normalization.
 *
 * <p>Contract: all email addresses in this service are trimmed and converted to lowercase
 * before persistence, uniqueness checks and authentication lookups (as documented in
 * api-contracts.md §1 business rules). This ensures that {@code User@Example.com} and
 * {@code user@example.com} are treated identically throughout the system.
 *
 * <p>Called in:
 * <ul>
 *   <li>{@code AuthService.register()} — before uniqueness check and setEmail</li>
 *   <li>{@code AuthService.login()} — before findByEmail lookup</li>
 *   <li>{@code OfficerService.createOfficer()} — before uniqueness check and setEmail</li>
 * </ul>
 *
 * <p>Owner: M1.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {}

    /**
     * Trims leading/trailing whitespace and converts to lowercase (Locale.ROOT).
     *
     * @param email raw email string, may be null
     * @return normalized email, or {@code null} if input is null
     */
    public static String normalize(String email) {
        return (email == null) ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
