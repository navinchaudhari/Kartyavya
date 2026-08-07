package com.kartyavya.gateway;

import com.kartyavya.contracts.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        EnvFileLoader.load();
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
