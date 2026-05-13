package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StudyPlanSchemaInitializer implements CommandLineRunner {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS study_plan (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "user_id BIGINT NOT NULL," +
                        "question_set_id BIGINT NOT NULL," +
                        "daily_count INT NOT NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_sp_user (user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        try {
            jdbcTemplate.execute("ALTER TABLE study_plan DROP INDEX uk_sp_user");
        } catch (Exception ignored) {
            // 兼容已移除索引或旧环境不存在该索引
        }

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS study_plan_record (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "plan_id BIGINT NOT NULL," +
                        "question_id BIGINT NOT NULL," +
                        "status TINYINT NOT NULL DEFAULT 0," +
                        "learned_time DATETIME NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_spr_plan_question (plan_id, question_id)," +
                        "INDEX idx_spr_plan_status (plan_id, status)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
