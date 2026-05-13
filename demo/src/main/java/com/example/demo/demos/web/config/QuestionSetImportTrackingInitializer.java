package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 为从公共题库导入的私有题库记录来源，用于统计「导入量」并参与热度排序（与在线试卷 copy_count 类似）。
 */
@Component
public class QuestionSetImportTrackingInitializer implements CommandLineRunner {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE question_set ADD COLUMN source_public_set_id BIGINT NULL COMMENT '来源公共题库ID（导入时写入）'"
            );
        } catch (Exception ignored) {
        }
    }
}
