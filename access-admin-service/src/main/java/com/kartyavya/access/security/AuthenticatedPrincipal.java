package com.kartyavya.access.security;

// Immutable principal stored in SecurityContextHolder after successful JWT validation.
// Carried as the principal of UsernamePasswordAuthenticationToken.
public record AuthenticatedPrincipal(
    Long userId,
    String email,
    String role,
    Long departmentId   // null for CITIZEN and ADMIN
) {}
