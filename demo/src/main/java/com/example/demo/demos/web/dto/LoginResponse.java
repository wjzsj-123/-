package com.example.demo.demos.web.dto;

import com.example.demo.demos.web.pojo.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
}
