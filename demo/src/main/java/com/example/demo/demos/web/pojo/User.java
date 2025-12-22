package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

