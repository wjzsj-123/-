package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QuestionSet {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于关联查询
    private User user;
}
