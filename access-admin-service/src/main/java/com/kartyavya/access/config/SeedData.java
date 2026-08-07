package com.kartyavya.access.config;

import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.RoutingRule;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.RoutingRuleRepository;
import com.kartyavya.access.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedData {
	@Bean
	CommandLineRunner seed(UserRepository users, DepartmentRepository departments, RoutingRuleRepository routingRules,
			PasswordEncoder encoder, @Value("${app.seed.admin.email:admin@kartyavya.local}") String adminEmail,
			@Value("${app.seed.admin.password}") String adminPassword,
			@Value("${app.seed.admin.full-name:System Administrator}") String adminName,
			@Value("${app.seed.admin.mobile:9999999999}") String adminMobile,
			@Value("${app.seed.admin.address:Municipal Corporation Office}") String adminAddress) {
		return args -> {
			if (!users.existsByEmailIgnoreCase(adminEmail)) {
				User admin = new User();
				admin.setFullName(adminName);
				admin.setEmail(adminEmail.toLowerCase());
				admin.setPasswordHash(encoder.encode(adminPassword));
				admin.setMobileNumber(adminMobile);
				admin.setAddress(adminAddress);
				admin.setRole("Admin");
				users.save(admin);
			}

			if (departments.count() == 0) {
				Department roads = department(departments, "Roads & Infrastructure", "roads@kartyavya.local",
						"Roads, potholes and public infrastructure");
				Department waste = department(departments, "Solid Waste", "waste@kartyavya.local",
						"Garbage collection and public sanitation");
				Department electrical = department(departments, "Electrical", "electrical@kartyavya.local",
						"Streetlights and public electrical infrastructure");
				Department water = department(departments, "Water & Drainage", "water@kartyavya.local",
						"Water leakage, drainage and pipeline issues");

				routingRule(routingRules, "POTHOLE", roads);
				routingRule(routingRules, "GARBAGE", waste);
				routingRule(routingRules, "STREETLIGHT", electrical);
				routingRule(routingRules, "WATER_LEAKAGE", water);
			}
		};
	}

	private static Department department(DepartmentRepository repository, String name, String email,
			String description) {
		Department department = new Department();
		department.setName(name);
		department.setContactEmail(email);
		department.setDescription(description);
		return repository.save(department);
	}

	private static void routingRule(RoutingRuleRepository repository, String category, Department department) {
		RoutingRule rule = new RoutingRule();
		rule.setCategory(category);
		rule.setDepartment(department);
		repository.save(rule);
	}
}
