package com.kartyavya.access.controller;

import com.kartyavya.access.entity.*;
import com.kartyavya.access.repository.*;
import com.kartyavya.contracts.*;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalController {
    private final UserRepository users;
    private final DepartmentRepository depts;
    private final OfficerAssignmentRepository assignments;
    private final RoutingRuleRepository rules;

    @GetMapping("/users/{id}/contact")
    AccessContracts.UserContact user(@PathVariable Long id) {
        User u = users.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        OfficerAssignment a = assignments.findByOfficerId(id).orElse(null);
        return new AccessContracts.UserContact(u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(),
                u.getRole(), a == null ? null : a.getDepartment().getId(),
                a == null ? null : a.getDepartment().getName(), u.isEnabled());
    }

    @GetMapping("/departments/{id}")
    AccessContracts.DepartmentInfo dept(@PathVariable Long id) {
        Department d = depts.findById(id).orElseThrow(() -> new NoSuchElementException("Department not found"));
        return new AccessContracts.DepartmentInfo(d.getId(), d.getName(), d.getContactEmail(), d.isEnabled());
    }

    @GetMapping("/departments/{id}/officers")
    AccessContracts.OfficersResponse officers(@PathVariable Long id) {
        return new AccessContracts.OfficersResponse(
                assignments.findByDepartmentIdAndActiveTrue(id).stream().filter(a -> a.getOfficer().isEnabled())
                        .map(a -> new AccessContracts.OfficerInfo(a.getOfficer().getId(), a.getOfficer().getFullName(),
                                a.getOfficer().getEmail(), a.getOfficer().getMobileNumber(), a.getDepartment().getId(),
                                a.getDepartment().getName(), true))
                        .toList());
    }

    @GetMapping("/officers/{id}")
    AccessContracts.OfficerInfo officer(@PathVariable Long id) {
        OfficerAssignment a = assignments.findByOfficerId(id)
                .orElseThrow(() -> new NoSuchElementException("Officer not found"));
        User u = a.getOfficer();
        return new AccessContracts.OfficerInfo(u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(),
                a.getDepartment().getId(), a.getDepartment().getName(), u.isEnabled() && a.isActive());
    }

    @GetMapping("/routing/resolve")
    AccessContracts.RoutingResolution route(@RequestParam String category) {
        RoutingRule r = rules.findByCategoryIgnoreCase(category).orElse(null);
        if (r == null)
            return new AccessContracts.RoutingResolution(false, category, null, null, null,
                    "No routing rule is configured for this AI category");
        if (!r.isActive() || !r.getDepartment().isEnabled())
            return new AccessContracts.RoutingResolution(false, category, null, null, null,
                    "The mapped department or routing rule is disabled");
        Department d = r.getDepartment();
        return new AccessContracts.RoutingResolution(true, r.getCategory(), d.getId(), d.getName(), d.getContactEmail(),
                null);
    }

    @GetMapping("/stats")
    Object stats() {
        return Map.of("totalCitizens", users.countByRoleAndEnabledTrue("Citizen"), "totalOfficers",
                users.countByRoleAndEnabledTrue("Officer"), "totalDepartments", depts.countByEnabledTrue());
    }
}
