package com.example.demo.demos.web.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-jwt-secret-key-at-least-32-chars");
        ReflectionTestUtils.setField(jwtService, "expirationDays", 7);
        jwtService.init();
    }

    @Test
    void generateAndParseToken_shouldRoundTripUserId() {
        String token = jwtService.generateToken(42L, "alice");

        assertNotNull(token);
        assertEquals(42L, jwtService.getUserId(token));
        assertEquals("alice", jwtService.parseToken(token).get("username"));
    }
}
