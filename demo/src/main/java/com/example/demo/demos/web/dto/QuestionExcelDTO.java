package com.example.demo.demos.web.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionExcelDTO {
    @ExcelProperty(value = "题目内容", index = 0)
    private String content;

    @ExcelProperty(value = "题目类型", index = 1)
    private String type; // 对应数据库中的type（1:单选/3:多选/2:填空）

    @ExcelProperty(value = "难度", index = 2)
    private Integer difficulty; // 1:简单/2:中等/3:困难

    @ExcelProperty(value = "选项A", index = 3)
    private String optionA;

    @ExcelProperty(value = "选项B", index = 4)
    private String optionB;

    @ExcelProperty(value = "选项C", index = 5)
    private String optionC;

    @ExcelProperty(value = "选项D", index = 6)
    private String optionD;

    @ExcelProperty(value = "正确答案", index = 7)
    private String correctAnswer; // 单选/多选存选项序号（A/B/C/D），判断存"对"/"错"，填空存具体文本
}