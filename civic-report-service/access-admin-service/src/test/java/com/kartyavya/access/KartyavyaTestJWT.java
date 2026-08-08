package com.kartyavya.access;

import com.kartyavya.access.entity.User;
import com.kartyavya.access.security.JwtService;

public class KartyavyaTestJWT {
    public static void main(String[] args) {
        JwtService jwtService = new JwtService("my-super-secret-key-which-must-be-very-long-to-be-secure", 60);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        String token = jwtService.generateToken(user, "CITIZEN", null);
        System.out.println("TOKEN:");
        System.out.println(token);
    }
}
