package com.example.demo.demos.web.config;

import com.alibaba.fastjson.JSON;
import com.example.demo.demos.web.auth.AuthContext;
import com.example.demo.demos.web.auth.JwtService;
import com.example.demo.demos.web.auth.TokenSessionService;
import com.example.demo.demos.web.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenSessionService tokenSessionService;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandle_shouldAllowPublicLoginPathWithoutToken() throws Exception {
        request.setRequestURI("/api/user/login");
        request.setMethod("POST");

        assertTrue(authInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_shouldRejectProtectedPathWithoutToken() throws Exception {
        request.setRequestURI("/api/question-set");
        request.setMethod("GET");

        assertFalse(authInterceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        Result<?> body = JSON.parseObject(response.getContentAsString(), Result.class);
        assertEquals(401, body.getCode());
    }

    @Test
    void preHandle_shouldAcceptValidBearerToken() throws Exception {
        request.setRequestURI("/api/question-set");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.getUserId("valid-token")).thenReturn(7L);
        when(tokenSessionService.isTokenValid(7L, "valid-token")).thenReturn(true);

        assertTrue(authInterceptor.preHandle(request, response, new Object()));
        assertEquals(7L, request.getAttribute(AuthContext.ATTR_USER_ID));
    }

    @Test
    void preHandle_shouldRejectInvalidSessionToken() throws Exception {
        request.setRequestURI("/api/paper");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer stale-token");
        when(jwtService.getUserId("stale-token")).thenReturn(7L);
        when(tokenSessionService.isTokenValid(7L, "stale-token")).thenReturn(false);

        assertFalse(authInterceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }
}
