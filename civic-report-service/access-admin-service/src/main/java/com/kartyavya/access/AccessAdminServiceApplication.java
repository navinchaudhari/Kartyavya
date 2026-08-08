package com.kartyavya.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Owner: M1 (Project Lead). Exclusive owner of access-admin-service/.
// Frozen contracts: docs/contracts/api-contracts.md, database-schema.md, feign-contracts.md (provider side).
@SpringBootApplication
public class AccessAdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccessAdminServiceApplication.class, args);
    }
}
