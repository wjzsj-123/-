package com.example.demo.demos.web.pojo;

import lombok.Data;

@Data
public class FillAnswer {
    private Long id;
    private Long questionId;
    private String answer;
    private Integer sortOrder;  // 对应题目中的第几个空
}
