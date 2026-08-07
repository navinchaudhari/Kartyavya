package com.kartyavya.access;

import com.kartyavya.contracts.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AccessAdminServiceApplication {
	public static void main(String[] args) {
		EnvFileLoader.load();
		SpringApplication.run(AccessAdminServiceApplication.class, args);
	}
}
