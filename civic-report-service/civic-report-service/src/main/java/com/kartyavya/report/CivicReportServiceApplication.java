package com.kartyavya.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Owner: M2. Exclusive owner of civic-report-service/.
// Frozen contracts: api-contracts.md, feign-contracts.md (consumer side), rabbitmq-events.md (publisher), database-schema.md.
@EnableFeignClients
@SpringBootApplication
public class CivicReportServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CivicReportServiceApplication.class, args);
    }
}
