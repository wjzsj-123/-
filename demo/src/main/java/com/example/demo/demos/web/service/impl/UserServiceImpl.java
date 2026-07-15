package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.mapper.UserMapper;
import com.example.demo.demos.web.service.PasswordService;
import com.example.demo.demos.web.service.UserService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordService passwordService;

    @Override
    public int addUser(User user) {
        encodePasswordIfPresent(user);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.insert(user);
    }

    @Override
    public int deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userMapper.deleteById(id);
    }

    @Override
    public int updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        preparePasswordForUpdate(user);
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return userMapper.selectByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }

        User user = userMapper.selectByUsername(username.trim());
        if (user == null) {
            return null;
        }

        String stored = user.getPassword();
        boolean matched;
        if (passwordService.isBcryptEncoded(stored)) {
            matched = passwordService.matches(rawPassword, stored);
        } else {
            matched = rawPassword.equals(stored);
            if (matched) {
                userMapper.updatePassword(user.getId(), passwordService.encode(rawPassword));
            }
        }

        if (!matched) {
            return null;
        }

        user.setPassword(null);
        return user;
    }

    private void encodePasswordIfPresent(User user) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!passwordService.isBcryptEncoded(user.getPassword())) {
            user.setPassword(passwordService.encode(user.getPassword()));
        }
    }

    /** 未传密码或空字符串时不更新 password 字段；传入明文则 BCrypt 编码 */
    private void preparePasswordForUpdate(User user) {
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(null);
            return;
        }
        if (!passwordService.isBcryptEncoded(user.getPassword())) {
            user.setPassword(passwordService.encode(user.getPassword()));
        }
    }
}
