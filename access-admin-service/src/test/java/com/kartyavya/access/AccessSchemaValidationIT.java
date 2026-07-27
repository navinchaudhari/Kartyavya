package com.kartyavya.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests that validate the live access_db schema.
 *
 * Strategy:
 *   - Spring context loads with ddl-auto=validate; if context starts, all entity→table mappings pass.
 *   - Tests inspect the schema via information_schema and raw SQL — never modify seeded data.
 *   - Tests that insert rows use try/finally to guarantee cleanup of their own rows.
 *   - Flyway is never called programmatically (no clean(), no migrate()) — schema must pre-exist.
 *   - Eureka disabled via properties; Config Server provides all other configuration.
 *
 * TODO (Day 6): swap to Testcontainers MySQL to add a fresh-DB migration-from-scratch test
 *               once Docker is available project-wide.
 */
@SpringBootTest(
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
class AccessSchemaValidationIT {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads_impliesHibernateValidatePassed() {
        // If this test runs, the Spring context loaded successfully,
        // which means Hibernate ddl-auto=validate confirmed all entity mappings
        // match the live access_db schema.
        assertThat(dataSource).isNotNull();
    }

    @Test
    void expectedTablesExist() {
        List<String> required = List.of(
            "users", "roles", "user_roles",
            "departments", "officer_department_assignments", "routing_rules"
        );
        for (String table : required) {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class, table);
            assertThat(count)
                .as("Table '%s' must exist in access_db", table)
                .isEqualTo(1);
        }
    }

    @Test
    void seededRolesPresent_citizenOfficerAdmin() {
        List<String> roles = jdbcTemplate.queryForList("SELECT name FROM roles ORDER BY name", String.class);
        assertThat(roles).contains("ADMIN", "CITIZEN", "DEPARTMENT_OFFICER");
    }

    @Test
    void uniqueConstraint_users_email_enforced() {
        String email = "schema-" + UUID.randomUUID() + "@kartyavya-test.local";
        jdbcTemplate.update(
            "INSERT INTO users (name, email, password_hash, enabled, created_at, updated_at) " +
            "VALUES ('SchTest', ?, 'hash', true, NOW(6), NOW(6))", email);
        try {
            assertThatThrownBy(() ->
                jdbcTemplate.update(
                    "INSERT INTO users (name, email, password_hash, enabled, created_at, updated_at) " +
                    "VALUES ('SchTest2', ?, 'hash', true, NOW(6), NOW(6))", email))
                .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbcTemplate.update("DELETE FROM users WHERE email = ?", email);
        }
    }

    @Test
    void uniqueConstraint_routingRules_category_enforced() {
        String deptEmail = "schema-dept-" + UUID.randomUUID() + "@kartyavya-test.local";
        String deptName = "SchDept-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
            "INSERT INTO departments (name, contact_email, enabled, created_at, updated_at) " +
            "VALUES (?, ?, true, NOW(6), NOW(6))", deptName, deptEmail);
        Long deptId = jdbcTemplate.queryForObject(
            "SELECT id FROM departments WHERE contact_email = ?", Long.class, deptEmail);

        String category = "SCH_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        jdbcTemplate.update(
            "INSERT INTO routing_rules (category, department_id, active, created_at, updated_at) " +
            "VALUES (?, ?, true, NOW(6), NOW(6))", category, deptId);
        try {
            assertThatThrownBy(() ->
                jdbcTemplate.update(
                    "INSERT INTO routing_rules (category, department_id, active, created_at, updated_at) " +
                    "VALUES (?, ?, true, NOW(6), NOW(6))", category, deptId))
                .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbcTemplate.update("DELETE FROM routing_rules WHERE category = ?", category);
            jdbcTemplate.update("DELETE FROM departments WHERE id = ?", deptId);
        }
    }

    @Test
    void foreignKey_officerDepartmentAssignment_nonExistentDepartment_rejected() {
        String email = "schema-fk-" + UUID.randomUUID() + "@kartyavya-test.local";
        jdbcTemplate.update(
            "INSERT INTO users (name, email, password_hash, enabled, created_at, updated_at) " +
            "VALUES ('FKUser', ?, 'hash', true, NOW(6), NOW(6))", email);
        Long userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE email = ?", Long.class, email);
        try {
            // No user_roles row inserted, so only user cleanup needed
            assertThatThrownBy(() ->
                jdbcTemplate.update(
                    "INSERT INTO officer_department_assignments " +
                    "(officer_id, department_id, assigned_at, active) " +
                    "VALUES (?, 999999999, NOW(6), true)", userId))
                .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            // FK insert failed — nothing to clean in officer_department_assignments
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        }
    }

    @Test
    void foreignKey_routingRule_nonExistentDepartment_rejected() {
        String category = "FK_RR_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        // Insert fails at the FK level — nothing committed, nothing to clean
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO routing_rules (category, department_id, active, created_at, updated_at) " +
                "VALUES (?, 999999999, true, NOW(6), NOW(6))", category))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
