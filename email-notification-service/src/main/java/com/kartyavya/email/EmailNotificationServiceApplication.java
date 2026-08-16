package com.kartyavya.email;

import com.kartyavya.contracts.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class EmailNotificationServiceApplication {
	public static void main(String[] args) {
		EnvFileLoader.load();
		SpringApplication.run(EmailNotificationServiceApplication.class, args);
	}
}
