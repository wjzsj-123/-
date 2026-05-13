package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserFollowSchemaInitializer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS user_follow ("
                        + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                        + "follower_id BIGINT NOT NULL,"
                        + "followee_id BIGINT NOT NULL,"
                        + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "UNIQUE KEY uk_user_follow (follower_id, followee_id),"
                        + "KEY idx_followee (followee_id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
