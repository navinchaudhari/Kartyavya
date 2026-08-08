package com.kartyavya.access;

import com.kartyavya.access.config.SeedData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("integration")
class RoutingSeedValidationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedData_departmentsMatchExactConstants() {
        assertDepartmentExists(SeedData.DEPT_ROADS, SeedData.EMAIL_ROADS);
        assertDepartmentExists(SeedData.DEPT_SANIT, SeedData.EMAIL_SANIT);
        assertDepartmentExists(SeedData.DEPT_WATER, SeedData.EMAIL_WATER);
        assertDepartmentExists(SeedData.DEPT_GENERAL, SeedData.EMAIL_GENERAL);
    }

    @Test
    void seedData_routingRulesMatchCategoryToDeptMapping() {
        SeedData.CATEGORY_TO_DEPT.forEach((category, expectedDeptName) -> {
            String sql = """
                SELECT d.name FROM routing_rules r 
                JOIN departments d ON r.department_id = d.id 
                WHERE r.category = ?
            """;
            String actualDeptName = jdbcTemplate.queryForObject(sql, String.class, category);
            assertThat(actualDeptName).isEqualTo(expectedDeptName);
        });
    }

    private void assertDepartmentExists(String name, String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM departments WHERE name = ? AND contact_email = ?",
                Integer.class, name, email);
        assertThat(count).isEqualTo(1);
    }
}
