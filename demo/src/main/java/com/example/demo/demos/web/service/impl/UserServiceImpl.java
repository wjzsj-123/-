package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.mapper.UserMapper;
import com.example.demo.demos.web.service.UserService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public int addUser(User user) {
        // 设置创建时间和更新时间
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
        // 更新时间戳
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
}
