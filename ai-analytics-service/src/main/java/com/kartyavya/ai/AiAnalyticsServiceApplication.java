package com.kartyavya.ai;

import com.kartyavya.contracts.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class AiAnalyticsServiceApplication {
    public static void main(String[] args) {
        EnvFileLoader.load();
        SpringApplication.run(AiAnalyticsServiceApplication.class, args);
    }
}
