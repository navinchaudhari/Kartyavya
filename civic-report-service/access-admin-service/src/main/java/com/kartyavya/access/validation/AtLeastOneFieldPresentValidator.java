package com.kartyavya.access.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.RecordComponent;

/**
 * Validator for {@link AtLeastOneFieldPresent}.
 *
 * <p>Uses Java 16+ record reflection ({@link Class#getRecordComponents()}) to inspect all
 * record components. Returns {@code true} if at least one component value is non-null.
 * Falls back to field-based reflection for non-record classes (future-proofing).
 *
 * <p>Returning {@code false} triggers a global (class-level) ConstraintViolation. The
 * message is then collected by {@code GlobalExceptionHandler} under the key "request".
 */
public class AtLeastOneFieldPresentValidator
        implements ConstraintValidator<AtLeastOneFieldPresent, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null objects are handled by @NotNull elsewhere
        }
        Class<?> clazz = value.getClass();
        try {
            if (clazz.isRecord()) {
                for (RecordComponent rc : clazz.getRecordComponents()) {
                    Object fieldValue = rc.getAccessor().invoke(value);
                    if (fieldValue != null) {
                        return true;
                    }
                }
            } else {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.get(value) != null) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            // Reflection failure — treat as valid to avoid masking real errors
            return true;
        }
        return false;
    }
}
