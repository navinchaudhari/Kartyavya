package com.kartyavya.access.exception;

/**
 * Thrown when a write operation targets a department that has {@code enabled=false}.
 * Applies to:
 * <ul>
 *   <li>{@code POST /api/admin/officers} — target department is disabled</li>
 *   <li>{@code POST /api/routing-rules} — target department is disabled</li>
 *   <li>{@code PATCH /api/routing-rules/{id}} — updated departmentId refers to a disabled department</li>
 * </ul>
 *
 * <p>HTTP mapping: 400 / INVALID_REQUEST / fieldErrors=null.
 *
 * <p>Note: {@code resolveByCategory} also handles disabled departments, but throws
 * {@link RoutingRuleNotFoundException} (404) instead, as the feign-contracts.md error
 * mapping for that path uses 404/ROUTING_RULE_NOT_FOUND.
 */
public class DepartmentDisabledException extends RuntimeException {

    public DepartmentDisabledException(String message) {
        super(message);
    }
}
