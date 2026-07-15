package com.example.demo.demos.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        passwordService = new PasswordService();
        ReflectionTestUtils.setField(passwordService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void encode_shouldProduceBcryptHash() {
        String encoded = passwordService.encode("secret123");
        assertTrue(passwordService.isBcryptEncoded(encoded));
        assertNotEquals("secret123", encoded);
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String encoded = passwordService.encode("secret123");
        assertTrue(passwordService.matches("secret123", encoded));
    }

    @Test
    void matches_shouldReturnFalseForWrongPassword() {
        String encoded = passwordService.encode("secret123");
        assertFalse(passwordService.matches("wrong", encoded));
    }

    @Test
    void isBcryptEncoded_shouldDetectPlainText() {
        assertFalse(passwordService.isBcryptEncoded("plain"));
    }
}
