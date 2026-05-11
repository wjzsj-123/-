package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CommentSchemaInitializer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS question_set_comment (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "question_set_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "content VARCHAR(500) NOT NULL," +
                        "sentiment TINYINT NOT NULL," +
                        "like_count INT NOT NULL DEFAULT 0," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "INDEX idx_qsc_set_time (question_set_id, create_time)," +
                        "INDEX idx_qsc_set_like (question_set_id, like_count)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS question_set_comment_like (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "comment_id BIGINT NOT NULL," +
                        "question_set_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_comment_user (comment_id, user_id)," +
                        "INDEX idx_qscl_set (question_set_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS question_set_vote (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "question_set_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "vote_type TINYINT NOT NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_qsv_set_user (question_set_id, user_id)," +
                        "INDEX idx_qsv_set_type (question_set_id, vote_type)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
