package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.UserMapper;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserServiceImpl userService;

    private User dbUser;

    @BeforeEach
    void setUp() {
        dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUsername("alice");
        dbUser.setPassword("$2a$10$encoded");
    }

    @Test
    void authenticate_shouldReturnUserWhenBcryptMatches() {
        when(userMapper.selectByUsername("alice")).thenReturn(dbUser);
        when(passwordService.isBcryptEncoded(dbUser.getPassword())).thenReturn(true);
        when(passwordService.matches("pass", dbUser.getPassword())).thenReturn(true);

        User result = userService.authenticate("alice", "pass");

        assertNotNull(result);
        assertNull(result.getPassword());
        verify(userMapper, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void authenticate_shouldMigratePlainPasswordToBcrypt() {
        dbUser.setPassword("plain");
        when(userMapper.selectByUsername("alice")).thenReturn(dbUser);
        when(passwordService.isBcryptEncoded("plain")).thenReturn(false);
        when(passwordService.encode("plain")).thenReturn("$2a$10$new");

        User result = userService.authenticate("alice", "plain");

        assertNotNull(result);
        verify(userMapper).updatePassword(1L, "$2a$10$new");
    }

    @Test
    void authenticate_shouldReturnNullWhenPasswordWrong() {
        when(userMapper.selectByUsername("alice")).thenReturn(dbUser);
        when(passwordService.isBcryptEncoded(dbUser.getPassword())).thenReturn(true);
        when(passwordService.matches("wrong", dbUser.getPassword())).thenReturn(false);

        assertNull(userService.authenticate("alice", "wrong"));
    }

    @Test
    void addUser_shouldEncodePlainPassword() {
        User user = new User();
        user.setUsername("bob");
        user.setPassword("123456");
        when(passwordService.isBcryptEncoded("123456")).thenReturn(false);
        when(passwordService.encode("123456")).thenReturn("$2a$10$hash");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.addUser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("$2a$10$hash", captor.getValue().getPassword());
        assertNotNull(captor.getValue().getCreateTime());
    }
}
