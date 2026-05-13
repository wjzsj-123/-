package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PaperSchemaInitializer implements CommandLineRunner {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE paper ADD COLUMN is_shared TINYINT(1) NOT NULL DEFAULT 0");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE paper ADD COLUMN share_code VARCHAR(64) NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("ALTER TABLE paper ADD COLUMN source_paper_id BIGINT NULL");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX uk_paper_share_code ON paper(share_code)");
        } catch (Exception ignored) {
        }
    }
}
