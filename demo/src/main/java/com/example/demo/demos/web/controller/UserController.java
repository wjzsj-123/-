package com.example.demo.demos.web.controller;

import com.example.demo.demos.web.auth.AuthContext;
import com.example.demo.demos.web.auth.JwtService;
import com.example.demo.demos.web.auth.TokenSessionService;
import com.example.demo.demos.web.common.Result;
import com.example.demo.demos.web.dto.LoginResponse;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.redis.LoginRateLimitService;
import com.example.demo.demos.web.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private LoginRateLimitService loginRateLimitService;

    @Resource
    private JwtService jwtService;

    @Resource
    private TokenSessionService tokenSessionService;

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
            if (user != null) {
                user.setPassword(null);
            }
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
            if (user != null) {
                user.setPassword(null);
            }
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
            users.forEach(u -> u.setPassword(null));
            return Result.success("查询成功", users);
        } catch (Exception e) {
            return Result.error("查询用户列表失败：" + e.getMessage());
        }
    }

    // 注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        int count = userService.addUser(user);
        return count > 0 ? Result.success("注册成功") : Result.error("注册失败");
    }

    // 登录接口
    @PostMapping("/login")
    public Result login(@RequestBody User user, HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        if (loginRateLimitService.isBlocked(clientIp)) {
            return Result.error("登录失败次数过多，请 15 分钟后再试");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名或密码错误");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名或密码错误");
        }
        User dbUser = userService.authenticate(user.getUsername(), user.getPassword());
        if (dbUser == null) {
            loginRateLimitService.recordFailure(clientIp);
            return Result.error("用户名或密码错误");
        }
        loginRateLimitService.clearFailures(clientIp);
        String token = jwtService.generateToken(dbUser.getId(), dbUser.getUsername());
        tokenSessionService.storeToken(dbUser.getId(), token);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setUser(dbUser);
        return Result.success("登录成功", loginResponse);
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
        if (userId != null) {
            tokenSessionService.removeToken(userId);
        }
        return Result.success("退出成功");
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}