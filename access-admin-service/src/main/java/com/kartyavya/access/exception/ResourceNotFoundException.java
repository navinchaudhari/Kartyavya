package com.kartyavya.access.exception;

/**
 * Thrown when a requested entity cannot be found by its ID.
 * HTTP mapping: 404 / RESOURCE_NOT_FOUND / fieldErrors=null.
 *
 * <p>Used by admin CRUD operations (DepartmentService, RoutingRuleService, UserAdminService,
 * OfficerService) when a findById lookup returns empty.
 *
 * <p>Note: RoutingRuleNotFoundException is used instead for the internal routing-resolve path
 * (feign-contracts.md maps that path's 404 to ROUTING_RULE_NOT_FOUND).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
