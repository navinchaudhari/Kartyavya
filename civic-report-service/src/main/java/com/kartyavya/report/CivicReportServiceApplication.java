package com.kartyavya.report;

import com.kartyavya.contracts.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class CivicReportServiceApplication {
    public static void main(String[] args) {
        EnvFileLoader.load();
        SpringApplication.run(CivicReportServiceApplication.class, args);
    }
}
