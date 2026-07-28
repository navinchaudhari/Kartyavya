package com.kartyavya.access.controller;

import com.kartyavya.access.dto.DepartmentRoutingResponse;
import com.kartyavya.access.service.RoutingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/routing")
@Tag(name = "Internal", description = "Internal service-to-service APIs")
@SecurityRequirement(name = "internalServiceKey")
public class InternalRoutingController {

    private final RoutingRuleService routingRuleService;

    public InternalRoutingController(RoutingRuleService routingRuleService) {
        this.routingRuleService = routingRuleService;
    }

    @GetMapping("/resolve")
    @Operation(summary = "Resolve a complaint category to a department (INTERNAL_SERVICE only)")
    public DepartmentRoutingResponse resolveByCategory(@RequestParam String category) {
        return routingRuleService.resolveByCategory(category);
    }
}
