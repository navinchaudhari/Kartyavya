package com.kartyavya.access.exception;

/**
 * Thrown when a routing category string is not one of the five frozen values:
 * POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER.
 *
 * <p>HTTP mapping: 400 / VALIDATION_FAILED / fieldErrors={"category":"must be one of POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER"}.
 */
public class InvalidCategoryException extends RuntimeException {

    public InvalidCategoryException(String category) {
        super("Invalid category: '" + category + "'. Must be one of POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER");
    }
}
