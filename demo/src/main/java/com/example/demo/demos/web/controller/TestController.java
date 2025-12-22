package com.example.demo.demos.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author,lwq
 * @Package,com.example.demo.demos.web.controller
 * @CreatTime,2025/10/9,下午3:26
 **/
@RestController
public class TestController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "Hello world";
    }
}
