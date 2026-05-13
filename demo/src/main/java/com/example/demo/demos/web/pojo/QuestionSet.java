package com.example.demo.demos.web.pojo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class QuestionSet {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于关联查询
    private User user;

    // 非数据库字段，用于统计
    private Integer choiceCount; // 单选题数量
    private Integer multiCount;  // 多选题数量
    private Integer fillCount;   // 填空题数量

    // 非数据库字段：公共题库展示用，聚合该题库下的所有标签
    private List<String> questionTags;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer favorability;
    private Integer userVote; // 当前用户投票状态：1点赞，-1点踩，0未投票

    /** 从公共题库导入时记录来源公共题库 ID，用于统计导入次数 */
    private Long sourcePublicSetId;

    /** 公共列表：被导入次数（非表字段，查询中聚合） */
    private Integer importCount;

    /** 公共列表：热度分 = 点赞*2 + 导入*3，与在线试卷 hot_score 规则一致 */
    private Integer hotScore;

    /** 发布者展示名（关联查询，非表字段） */
    private String publisherNickname;
    private String publisherUsername;
    /** 当前用户是否已关注发布者（登录且传 currentUserId 时有效） */
    private Boolean viewerFollowsPublisher;

    /** 题库管理：发布者粉丝数（被关注数），仅公开题库有统计意义 */
    private Integer publisherFollowerCount;

    // 公共题库字段
    private Integer isPublic; // 0-私有，1-公共
    private Long publisherId; // 发布者ID
    private LocalDateTime publishTime;
}
