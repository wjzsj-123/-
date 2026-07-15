package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.auth.JwtService;
import com.example.demo.demos.web.auth.TokenSessionService;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.redis.LoginRateLimitService;
import com.example.demo.demos.web.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private LoginRateLimitService loginRateLimitService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenSessionService tokenSessionService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() throws Exception {
        User loginUser = new User();
        loginUser.setUsername("alice");
        loginUser.setPassword("secret");

        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUsername("alice");

        when(loginRateLimitService.isBlocked(anyString())).thenReturn(false);
        when(userService.authenticate("alice", "secret")).thenReturn(dbUser);
        when(jwtService.generateToken(1L, "alice")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("alice"));

        verify(tokenSessionService).storeToken(eq(1L), eq("jwt-token"));
        verify(loginRateLimitService).clearFailures(anyString());
    }

    @Test
    void login_shouldReturnErrorWhenCredentialsInvalid() throws Exception {
        User loginUser = new User();
        loginUser.setUsername("alice");
        loginUser.setPassword("wrong");

        when(loginRateLimitService.isBlocked(anyString())).thenReturn(false);
        when(userService.authenticate("alice", "wrong")).thenReturn(null);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(loginRateLimitService).recordFailure(anyString());
        verify(tokenSessionService, never()).storeToken(any(), anyString());
    }

    @Test
    void register_shouldRejectDuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("secret");

        User existing = new User();
        existing.setId(2L);
        when(userService.getUserByUsername("alice")).thenReturn(existing);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }
}
