package com.example.demo.demos.web.controller;

// 后端添加一个 Controller，统一重定向前端路由
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    // 匹配所有前端路由，重定向到 index.html（让 Vue Router 处理路由）
    @GetMapping(value = {"/", "/login", "/register", "/home/**"})
    public String redirectToIndex() {
        return "forward:/index.html";
    }
}