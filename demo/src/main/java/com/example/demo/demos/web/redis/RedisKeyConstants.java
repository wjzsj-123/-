package com.example.demo.demos.web.redis;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    public static final String MSG_UNREAD_PREFIX = "msg:unread:";
    public static final String PUBLIC_QSET_LIST_PREFIX = "cache:public:qset:list:";
    public static final String PUBLIC_PAPER_LIST_PREFIX = "cache:public:paper:list:";
    public static final String DRAFT_PAPER_PREFIX = "draft:paper:";
    public static final String LOGIN_RATE_LIMIT_PREFIX = "ratelimit:login:";
    public static final String AUTH_TOKEN_PREFIX = "auth:token:";
    public static final String RANK_QSET_HOT = "rank:public:qset:hot";
    public static final String RANK_PAPER_HOT = "rank:public:paper:hot";
    public static final String VOTE_RATE_LIMIT_PREFIX = "ratelimit:vote:";
    public static final String COMMENT_RATE_LIMIT_PREFIX = "ratelimit:comment:";
    public static final String IMPORT_LOCK_PREFIX = "lock:import:";

    public static String msgUnread(Long userId) {
        return MSG_UNREAD_PREFIX + userId;
    }

    public static String publicQuestionSetList(String category, String name, String sortBy,
                                               int page, int size, Long currentUserId) {
        return PUBLIC_QSET_LIST_PREFIX
                + nullToToken(category) + ":"
                + nullToToken(name) + ":"
                + sortBy + ":"
                + page + ":"
                + size + ":"
                + (currentUserId != null ? currentUserId : "guest");
    }

    public static String publicPaperList(String name, String sortBy, int page, int size, Long currentUserId) {
        return PUBLIC_PAPER_LIST_PREFIX
                + nullToToken(name) + ":"
                + sortBy + ":"
                + page + ":"
                + size + ":"
                + (currentUserId != null ? currentUserId : "guest");
    }

    public static String answerDraft(Long paperId, Long userId) {
        return DRAFT_PAPER_PREFIX + paperId + ":user:" + userId;
    }

    public static String loginRateLimit(String ip) {
        return LOGIN_RATE_LIMIT_PREFIX + ip;
    }

    public static String authToken(Long userId) {
        return AUTH_TOKEN_PREFIX + userId;
    }

    public static String voteRateLimit(Long userId, Long setId) {
        return VOTE_RATE_LIMIT_PREFIX + userId + ":" + setId;
    }

    public static String commentRateLimit(Long userId) {
        return COMMENT_RATE_LIMIT_PREFIX + userId;
    }

    public static String importLock(Long userId) {
        return IMPORT_LOCK_PREFIX + userId;
    }

    private static String nullToToken(String value) {
        return value == null || value.trim().isEmpty() ? "all" : value.trim();
    }
}
