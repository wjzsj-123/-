package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.User;

import java.util.List;

/**
 * @Author,lwq
 * @Package,com.example.demo.demos.web.service
 * @CreatTime,2025/11/3,下午7:28
 **/
public interface UserService {
    int addUser(User user);

    int deleteUser(Long id);

    int updateUser(User user);

    User getUserById(Long id);

    User getUserByUsername(String username);

    List<User> getAllUsers();
}
