package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    // 新增用户
    @PostMapping
    public Result addUser(@RequestBody User user) {
        try {
            // 简单参数校验
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            // 检查用户名是否已存在
            User existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }
            int count = userService.addUser(user);
            return Result.success("新增用户成功", count);
        } catch (Exception e) {
            return Result.error("新增用户失败：" + e.getMessage());
        }
    }

    // 根据ID删除用户
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        try {
            if (id == null) {
                return Result.error("用户ID不能为空");
            }
            int count = userService.deleteUser(id);
            return count > 0 ? Result.success("删除用户成功") : Result.error("未找到该用户");
        } catch (Exception e) {
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping
    public Result updateUser(@RequestBody User user) {
        try {
            if (user.getId() == null) {
                return Result.error("用户ID不能为空");
            }
            // 检查用户名是否已被其他用户使用
            User existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                return Result.error("用户名已被使用");
            }
            int count = userService.updateUser(user);
            return count > 0 ? Result.success("更新用户成功") : Result.error("未找到该用户或无变更");
        } catch (Exception e) {
            return Result.error("更新用户失败：" + e.getMessage());
        }
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id) {
        try {
            if (id == null) {
                return Result.error("用户ID不能为空");
            }
            User user = userService.getUserById(id);
            return user != null ? Result.success("查询成功", user) : Result.error("未找到该用户");
        } catch (Exception e) {
            return Result.error("查询用户失败：" + e.getMessage());
        }
    }

    // 根据用户名查询用户
    @GetMapping("/username/{username}")
    public Result getUserByUsername(@PathVariable String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            User user = userService.getUserByUsername(username);
            return user != null ? Result.success("查询成功", user) : Result.error("未找到该用户");
        } catch (Exception e) {
            return Result.error("查询用户失败：" + e.getMessage());
        }
    }

    // 查询所有用户
    @GetMapping
    public Result getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return Result.success("查询成功", users);
        } catch (Exception e) {
            return Result.error("查询用户列表失败：" + e.getMessage());
        }
    }

    // 注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        int count = userService.addUser(user);
        return count > 0 ? Result.success("注册成功") : Result.error("注册失败");
    }

    // 登录接口
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User dbUser = userService.getUserByUsername(user.getUsername());
        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        return Result.success("登录成功", dbUser);
    }
}