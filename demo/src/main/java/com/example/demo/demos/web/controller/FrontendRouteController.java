package com.example.demo.demos.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端路由转发控制器：适配Vite构建产物（dist目录）
 */
@Controller
public class FrontendRouteController {

    // 匹配所有前端路由请求，转发到dist下的index.html（构建产物入口）
    @GetMapping(value = {"/", "/login", "/register", "/home/**"})
    public String forwardToDistIndex() {
        // 指向static/dist/index.html（Spring Boot自动拼接static-locations）
        return "forward:/dist/index.html";
    }
}