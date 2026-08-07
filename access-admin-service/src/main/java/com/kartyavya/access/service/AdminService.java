package com.kartyavya.access.service;

import com.kartyavya.access.dto.AdminDtos.*;
import com.kartyavya.access.entity.*;
import com.kartyavya.access.repository.*;
import com.kartyavya.contracts.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final DepartmentRepository depts;
	private final UserRepository users;
	private final OfficerAssignmentRepository assignments;
	private final RoutingRuleRepository rules;
	private final PasswordEncoder encoder;


	@Transactional(readOnly = true)
	public List<DepartmentResponse> departments(boolean onlyEnabled) {
		return (onlyEnabled ? depts.findByEnabledTrueOrderByNameAsc() : depts.findAll()).stream().map(this::dept)
				.toList();
	}

	@Transactional
	public DepartmentResponse createDepartment(DepartmentRequest r) {
		if (depts.existsByNameIgnoreCase(r.departmentName()))
			throw new IllegalStateException("Department already exists");
		Department d = new Department();
		apply(d, r);
		return dept(depts.save(d));
	}

	@Transactional
	public DepartmentResponse updateDepartment(Long id, DepartmentRequest r) {
		Department d = depts.findById(id).orElseThrow(() -> new NoSuchElementException("Department not found"));
		apply(d, r);
		return dept(depts.save(d));
	}

	@Transactional
	public void disableDepartment(Long id) {
		Department d = depts.findById(id).orElseThrow(() -> new NoSuchElementException("Department not found"));
		d.setEnabled(false);
		depts.save(d);
	}

	private void apply(Department d, DepartmentRequest r) {
		d.setName(r.departmentName().trim());
		d.setContactEmail(r.contactEmail().trim().toLowerCase());
		d.setDescription(r.description());
		if (r.enabled() != null)
			d.setEnabled(r.enabled());
	}

	private DepartmentResponse dept(Department d) {
		return new DepartmentResponse(d.getId(), d.getName(), d.getContactEmail(), d.getDescription(), d.isEnabled());
	}

	@Transactional(readOnly = true)
	public List<OfficerResponse> officers() {
		return assignments.findByActiveTrueOrderByOfficerFullNameAsc().stream().map(this::officer).toList();
	}

	@Transactional
	public OfficerResponse createOfficer(OfficerCreate r) {
		if (users.existsByEmailIgnoreCase(r.email()))
			throw new IllegalStateException("Email already registered");
		Department d = activeDept(r.departmentId());
		User u = new User();
		u.setFullName(r.fullName().trim());
		u.setEmail(r.email().trim().toLowerCase());
		u.setPasswordHash(encoder.encode(r.password()));
		u.setMobileNumber(r.mobileNumber());
		u.setAddress(r.address());
		u.setRole("Officer");
		users.save(u);
		OfficerAssignment a = new OfficerAssignment();
		a.setOfficer(u);
		a.setDepartment(d);
		assignments.save(a);
		return officer(a);
	}

	@Transactional
	public OfficerResponse updateOfficer(Long id, OfficerUpdate r) {
		User u = users.findById(id).filter(x -> "Officer".equals(x.getRole()))
				.orElseThrow(() -> new NoSuchElementException("Officer not found"));
		Department d = activeDept(r.departmentId());
		u.setFullName(r.fullName().trim());
		u.setMobileNumber(r.mobileNumber());
		u.setAddress(r.address());
		if (r.enabled() != null)
			u.setEnabled(r.enabled());
		OfficerAssignment a = assignments.findByOfficerId(id).orElseThrow();
		a.setDepartment(d);
		a.setActive(u.isEnabled());
		users.save(u);
		assignments.save(a);
		return officer(a);
	}

	@Transactional
	public void disableOfficer(Long id) {
		User u = users.findById(id).filter(x -> "Officer".equals(x.getRole()))
				.orElseThrow(() -> new NoSuchElementException("Officer not found"));
		u.setEnabled(false);
		assignments.findByOfficerId(id).ifPresent(a -> {
			a.setActive(false);
			assignments.save(a);
		});
		users.save(u);
	}

	private OfficerResponse officer(OfficerAssignment a) {
		User u = a.getOfficer();
		return new OfficerResponse(u.getId(), u.getFullName(), u.getEmail(), u.getMobileNumber(), u.getAddress(),
				a.getDepartment().getId(), a.getDepartment().getName(), u.isEnabled() && a.isActive());
	}

	private Department activeDept(Long id) {
		Department d = depts.findById(id).orElseThrow(() -> new NoSuchElementException("Department not found"));
		if (!d.isEnabled())
			throw new IllegalStateException("Department is disabled");
		return d;
	}

	@Transactional(readOnly = true)
	public List<RoutingRuleResponse> rules() {
		return rules.findAllByOrderByCategoryAsc().stream().map(this::rule).toList();
	}

	@Transactional
	public RoutingRuleResponse saveRule(Long id, RoutingRuleRequest r) {
		ReportCategory.valueOf(r.category().toUpperCase());
		Department d = activeDept(r.departmentId());
		RoutingRule x = id == null ? rules.findByCategoryIgnoreCase(r.category()).orElseGet(RoutingRule::new)
				: rules.findById(id).orElseThrow(() -> new NoSuchElementException("Routing rule not found"));
		x.setCategory(r.category().toUpperCase());
		x.setDepartment(d);
		x.setActive(r.active() == null || r.active());
		return rule(rules.save(x));
	}

	private RoutingRuleResponse rule(RoutingRule x) {
		return new RoutingRuleResponse(x.getId(), x.getCategory(), x.getDepartment().getId(),
				x.getDepartment().getName(), x.isActive());
	}

	@Transactional(readOnly = true)
	public List<UserResponse> citizens() {
		return users.findByRoleOrderByFullNameAsc("Citizen").stream().map(u -> new UserResponse(u.getId(),
				u.getFullName(), u.getEmail(), u.getMobileNumber(), u.getAddress(), u.getRole(), u.isEnabled()))
				.toList();
	}
}
