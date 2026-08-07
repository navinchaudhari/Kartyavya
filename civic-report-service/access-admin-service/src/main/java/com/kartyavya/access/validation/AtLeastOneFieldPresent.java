package com.kartyavya.access.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Class-level constraint that requires at least one record component (or bean property)
 * to be non-null. Used on partial-update request DTOs (e.g. {@code DepartmentUpdateRequest},
 * {@code RoutingRuleUpdateRequest}) to reject all-null PATCH bodies.
 *
 * <p>Validation errors from this constraint appear in {@code fieldErrors.request} (the literal
 * key "request") in the canonical {@code ApiErrorResponse}, as set by
 * {@code GlobalExceptionHandler.handleValidation()}.
 */
@Documented
@Constraint(validatedBy = AtLeastOneFieldPresentValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneFieldPresent {

    String message() default "At least one field must be non-null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
