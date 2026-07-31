package com.kartyavya.report.integration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.kartyavya.report.integration.dto.UserContactResponse;

@FeignClient(name = "access-admin-service")
public interface UserClient {

    @GetMapping("/internal/users/{userId}/contact")
    UserContactResponse getUserContact(
            @PathVariable("userId") Long userId
    );
}