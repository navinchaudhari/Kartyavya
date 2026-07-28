package com.kartyavya.access.exception;

/**
 * Thrown by {@code RoutingRuleService.resolveByCategory()} when:
 * <ul>
 *   <li>No routing rule exists for the given category, or</li>
 *   <li>The rule exists but {@code active=false}, or</li>
 *   <li>The rule's department is disabled (disabled dept → no usable route).</li>
 * </ul>
 *
 * <p>HTTP mapping: 404 / ROUTING_RULE_NOT_FOUND / fieldErrors=null.
 * This mapping is frozen in docs/contracts/feign-contracts.md for the internal
 * {@code GET /internal/routing/resolve} endpoint consumed by civic-report-service (M2).
 *
 * <p>Intentionally separate from {@link ResourceNotFoundException} (RESOURCE_NOT_FOUND/404)
 * to give the Feign consumer a distinct, actionable error code.
 */
public class RoutingRuleNotFoundException extends RuntimeException {

    public RoutingRuleNotFoundException(String message) {
        super(message);
    }
}
