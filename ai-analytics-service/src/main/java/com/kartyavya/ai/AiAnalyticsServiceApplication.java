package com.kartyavya.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Owner: M3. Exclusive owner of ai-analytics-service/.
// Frozen contracts: api-contracts.md, rabbitmq-events.md (consumer + publisher), database-schema.md (Mongo).
@SpringBootApplication
public class AiAnalyticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAnalyticsServiceApplication.class, args);
    }
}
