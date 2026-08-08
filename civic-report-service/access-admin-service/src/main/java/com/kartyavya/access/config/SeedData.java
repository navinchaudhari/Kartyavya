package com.kartyavya.access.config;

import java.util.Map;

/**
 * Single source of truth for the four frozen department names and their contact emails,
 * and the five frozen category→department routing mappings.
 *
 * <p>The corresponding SQL lives in V2__seed_departments_and_routing_rules.sql.
 * Any drift between this file and the SQL will be caught by RoutingSeedValidationIT.
 *
 * <p>Owner: M1. Never retype these literal strings anywhere else.
 */
public final class SeedData {

    // Department names — must match V2 migration verbatim (case-sensitive)
    public static final String DEPT_ROADS   = "Roads & Infrastructure";
    public static final String DEPT_SANIT   = "Sanitation";
    public static final String DEPT_WATER   = "Water Supply";
    public static final String DEPT_GENERAL = "General Administration";

    // Contact emails — must match V2 migration verbatim
    public static final String EMAIL_ROADS   = "roads@kartyavya.local";
    public static final String EMAIL_SANIT   = "sanitation@kartyavya.local";
    public static final String EMAIL_WATER   = "water@kartyavya.local";
    public static final String EMAIL_GENERAL = "general@kartyavya.local";

    /**
     * Canonical mapping of frozen routing category → owning department name.
     * Categories are the exact strings used in routing_rules.category (UPPER_SNAKE_CASE).
     * RoutingSeedValidationIT iterates this map to verify DB state after V2 runs.
     */
    public static final Map<String, String> CATEGORY_TO_DEPT = Map.of(
        "POTHOLE",       DEPT_ROADS,
        "STREETLIGHT",   DEPT_ROADS,
        "GARBAGE",       DEPT_SANIT,
        "WATER_LEAKAGE", DEPT_WATER,
        "OTHER",         DEPT_GENERAL
    );

    private SeedData() {}
}
