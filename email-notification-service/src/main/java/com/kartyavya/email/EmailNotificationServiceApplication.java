package com.kartyavya.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

// Owner: M4. Exclusive owner of email-notification-service/.
// Frozen contracts: rabbitmq-events.md (consumer), feign-contracts.md (UserContactClient), database-schema.md (Mongo).
@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class EmailNotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailNotificationServiceApplication.class, args);
    }
}
