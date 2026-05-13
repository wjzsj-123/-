package com.example.demo.demos.web.pojo;

import lombok.Data;

import java.util.List;

/**
 * 用户中心聚合数据（不含密码）
 */
@Data
public class UserCenterDTO {
    private Long id;
    private String username;
    private String nickname;
    /** 仅本人查看时返回 */
    private String email;
    private int followingCount;
    private int followerCount;
    private boolean ownProfile;
    /** 浏览他人主页时：当前登录用户是否已关注该用户 */
    private Boolean viewerFollows;
    private List<QuestionSet> publicQuestionSets;
    private List<Paper> publicPapers;
}
