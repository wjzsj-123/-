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

    /**
     * 登录校验；若库中为旧版明文密码且校验通过，会自动升级为 BCrypt。
     *
     * @return 成功时返回用户（password 已置 null），失败返回 null
     */
    User authenticate(String username, String rawPassword);
}
