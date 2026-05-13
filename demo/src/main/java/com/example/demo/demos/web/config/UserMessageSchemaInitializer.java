package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserMessageSchemaInitializer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS user_message ("
                        + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                        + "user_id BIGINT NOT NULL COMMENT '接收者',"
                        + "type VARCHAR(64) NOT NULL,"
                        + "title VARCHAR(255) NOT NULL,"
                        + "content_preview VARCHAR(500) NULL,"
                        + "ref_question_set_id BIGINT NULL,"
                        + "ref_paper_id BIGINT NULL,"
                        + "ref_comment_id BIGINT NULL,"
                        + "actor_user_id BIGINT NULL,"
                        + "read_at DATETIME NULL,"
                        + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "KEY idx_um_user_time (user_id, created_at),"
                        + "KEY idx_um_user_read (user_id, read_at)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
