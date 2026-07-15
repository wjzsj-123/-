package com.example.demo.demos.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PasswordService {

    @Resource
    private PasswordEncoder passwordEncoder;

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /** BCrypt 哈希以 $2a$ / $2b$ / $2y$ 开头 */
    public boolean isBcryptEncoded(String stored) {
        return stored != null
                && (stored.startsWith("$2a$")
                || stored.startsWith("$2b$")
                || stored.startsWith("$2y$"));
    }
}
