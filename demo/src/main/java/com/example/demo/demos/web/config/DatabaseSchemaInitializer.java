package com.example.demo.demos.web.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 应用启动时统一自检数据库表结构：CREATE TABLE IF NOT EXISTS + 补列/补索引。
 * 覆盖本项目全部业务表，避免手工建表遗漏或版本升级缺列。
 */
@Component
@Order(1)
public class DatabaseSchemaInitializer implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        initUser();
        initQuestionSet();
        initQuestion();
        initQuestionOption();
        initFillAnswer();
        initPaper();
        initPaperQuestion();
        initUserAnswer();
        initStudyPlan();
        initStudyPlanRecord();
        initQuestionSetComment();
        initQuestionSetCommentLike();
        initQuestionSetVote();
        initUserFollow();
        initUserMessage();
    }

    private void exec(String sql) {
        jdbcTemplate.execute(sql);
    }

    private boolean tableExists(String table) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, table);
        return n != null && n > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return n != null && n > 0;
    }

    private void addColumn(String table, String column, String definition) {
        if (!columnExists(table, column)) {
            exec("ALTER TABLE `" + table + "` ADD COLUMN " + definition);
        }
    }

    private boolean indexExists(String table, String indexName) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class, table, indexName);
        return n != null && n > 0;
    }

    private void dropIndexIfExists(String table, String indexName) {
        if (indexExists(table, indexName)) {
            try {
                exec("ALTER TABLE `" + table + "` DROP INDEX `" + indexName + "`");
            } catch (Exception ignored) {
            }
        }
    }

    private void addIndex(String table, String indexName, String ddl) {
        if (!indexExists(table, indexName)) {
            try {
                exec(ddl);
            } catch (Exception ignored) {
            }
        }
    }

    private void initUser() {
        exec(
                "CREATE TABLE IF NOT EXISTS `user` (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "username VARCHAR(64) NOT NULL," +
                        "password VARCHAR(128) NOT NULL," +
                        "nickname VARCHAR(64) NULL," +
                        "email VARCHAR(128) NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_user_username (username)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    private void initQuestionSet() {
        exec(
                "CREATE TABLE IF NOT EXISTS question_set (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(255) NOT NULL," +
                        "description VARCHAR(500) NULL," +
                        "category VARCHAR(64) NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "is_public TINYINT NOT NULL DEFAULT 0," +
                        "publisher_id BIGINT NULL," +
                        "publish_time DATETIME NULL," +
                        "source_public_set_id BIGINT NULL COMMENT '来源公共题库ID'," +
                        "INDEX idx_qs_user (user_id)," +
                        "INDEX idx_qs_public (is_public, publish_time)," +
                        "INDEX idx_qs_source (source_public_set_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        if (tableExists("question_set")) {
            addColumn("question_set", "is_public", "is_public TINYINT NOT NULL DEFAULT 0");
            addColumn("question_set", "publisher_id", "publisher_id BIGINT NULL");
            addColumn("question_set", "publish_time", "publish_time DATETIME NULL");
            addColumn("question_set", "source_public_set_id",
                    "source_public_set_id BIGINT NULL COMMENT '来源公共题库ID（导入时写入）'");
        }
    }

    private void initQuestion() {
        exec(
                "CREATE TABLE IF NOT EXISTS question (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "question_set_id BIGINT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "tag VARCHAR(255) NULL," +
                        "type TINYINT NOT NULL," +
                        "difficulty TINYINT NOT NULL DEFAULT 1," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "INDEX idx_question_set (question_set_id)," +
                        "INDEX idx_question_set_type (question_set_id, type)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        if (tableExists("question")) {
            addColumn("question", "tag", "tag VARCHAR(255) NULL");
        }
    }

    private void initQuestionOption() {
        exec(
                "CREATE TABLE IF NOT EXISTS question_option (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "question_id BIGINT NOT NULL," +
                        "content VARCHAR(500) NOT NULL," +
                        "is_correct TINYINT NOT NULL DEFAULT 0," +
                        "sort_order INT NOT NULL DEFAULT 0," +
                        "INDEX idx_qo_question (question_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    private void initFillAnswer() {
        exec(
                "CREATE TABLE IF NOT EXISTS fill_answer (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "question_id BIGINT NOT NULL," +
                        "answer VARCHAR(500) NOT NULL," +
                        "sort_order INT NOT NULL DEFAULT 1," +
                        "INDEX idx_fa_question (question_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    private void initPaper() {
        exec(
                "CREATE TABLE IF NOT EXISTS paper (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "title VARCHAR(255) NOT NULL," +
                        "description VARCHAR(500) NULL," +
                        "user_id BIGINT NOT NULL," +
                        "category VARCHAR(64) NULL," +
                        "total_questions INT NOT NULL DEFAULT 0," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "last_answer_time DATETIME NULL," +
                        "is_answered TINYINT NOT NULL DEFAULT 0," +
                        "is_shared TINYINT(1) NOT NULL DEFAULT 0," +
                        "share_code VARCHAR(64) NULL," +
                        "source_paper_id BIGINT NULL," +
                        "INDEX idx_paper_user (user_id)," +
                        "INDEX idx_paper_shared (is_shared, create_time)," +
                        "INDEX idx_paper_source (source_paper_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        if (tableExists("paper")) {
            addColumn("paper", "description", "description VARCHAR(500) NULL");
            addColumn("paper", "category", "category VARCHAR(64) NULL");
            addColumn("paper", "last_answer_time", "last_answer_time DATETIME NULL");
            addColumn("paper", "is_answered", "is_answered TINYINT NOT NULL DEFAULT 0");
            addColumn("paper", "is_shared", "is_shared TINYINT(1) NOT NULL DEFAULT 0");
            addColumn("paper", "share_code", "share_code VARCHAR(64) NULL");
            addColumn("paper", "source_paper_id", "source_paper_id BIGINT NULL");
            addIndex("paper", "uk_paper_share_code",
                    "CREATE UNIQUE INDEX uk_paper_share_code ON paper(share_code)");
        }
    }

    private void initPaperQuestion() {
        exec(
                "CREATE TABLE IF NOT EXISTS paper_question (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "paper_id BIGINT NOT NULL," +
                        "question_id BIGINT NOT NULL," +
                        "sort_order INT NOT NULL DEFAULT 0," +
                        "INDEX idx_pq_paper (paper_id)," +
                        "INDEX idx_pq_question (question_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    private void initUserAnswer() {
        exec(
                "CREATE TABLE IF NOT EXISTS user_answer (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "user_id BIGINT NOT NULL," +
                        "paper_id BIGINT NOT NULL," +
                        "question_id BIGINT NOT NULL," +
                        "question_type TINYINT NOT NULL," +
                        "choice_option_id BIGINT NULL," +
                        "fill_content VARCHAR(500) NULL," +
                        "sort_order INT NULL," +
                        "is_correct TINYINT NULL," +
                        "submit_time DATETIME NULL," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "INDEX idx_ua_paper_user (paper_id, user_id)," +
                        "INDEX idx_ua_user (user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        if (tableExists("user_answer")) {
            addColumn("user_answer", "sort_order", "sort_order INT NULL");
        }
    }

    private void initStudyPlan() {
        exec(
                "CREATE TABLE IF NOT EXISTS study_plan (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "user_id BIGINT NOT NULL," +
                        "question_set_id BIGINT NOT NULL," +
                        "daily_count INT NOT NULL," +
                        "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        dropIndexIfExists("study_plan", "uk_sp_user");
        addIndex("study_plan", "uk_sp_user_set",
                "ALTER TABLE study_plan ADD UNIQUE KEY uk_sp_user_set (user_id, question_set_id)");
        addIndex("study_plan", "idx_sp_user",
                "ALTER TABLE study_plan ADD INDEX idx_sp_user (user_id)");
    }

    private void initStudyPlanRecord() {
        exec(
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

    private void initQuestionSetComment() {
        exec(
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
    }

    private void initQuestionSetCommentLike() {
        exec(
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
    }

    private void initQuestionSetVote() {
        exec(
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

    private void initUserFollow() {
        exec(
                "CREATE TABLE IF NOT EXISTS user_follow (" +
                        "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "follower_id BIGINT NOT NULL," +
                        "followee_id BIGINT NOT NULL," +
                        "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_user_follow (follower_id, followee_id)," +
                        "KEY idx_followee (followee_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }

    private void initUserMessage() {
        exec(
                "CREATE TABLE IF NOT EXISTS user_message (" +
                        "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "user_id BIGINT NOT NULL COMMENT '接收者'," +
                        "type VARCHAR(64) NOT NULL," +
                        "title VARCHAR(255) NOT NULL," +
                        "content_preview VARCHAR(500) NULL," +
                        "ref_question_set_id BIGINT NULL," +
                        "ref_paper_id BIGINT NULL," +
                        "ref_comment_id BIGINT NULL," +
                        "actor_user_id BIGINT NULL," +
                        "read_at DATETIME NULL," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_um_user_time (user_id, created_at)," +
                        "KEY idx_um_user_read (user_id, read_at)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
    }
}
