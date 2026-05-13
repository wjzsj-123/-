package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.UserFollowMapper;
import com.example.demo.demos.web.mapper.UserMessageMapper;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.pojo.UserMessage;
import com.example.demo.demos.web.service.UserMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    public static final String TYPE_FOLLOW_PUBLISH_SET = "FOLLOW_PUBLISH_SET";
    public static final String TYPE_FOLLOW_PUBLISH_PAPER = "FOLLOW_PUBLISH_PAPER";
    public static final String TYPE_DISCUSSION_COMMENT = "DISCUSSION_COMMENT";

    @Resource
    private UserMessageMapper userMessageMapper;
    @Resource
    private UserFollowMapper userFollowMapper;

    private static String preview(String text, int max) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyFollowersNewPublicQuestionSet(Long publisherId, Long setId, String setName) {
        if (publisherId == null || setId == null) {
            return;
        }
        List<User> followers = userFollowMapper.selectFollowerUsers(publisherId);
        if (followers == null || followers.isEmpty()) {
            return;
        }
        String pv = preview(setName != null ? setName : "题库", 200);
        for (User follower : followers) {
            if (follower == null || follower.getId() == null || follower.getId().equals(publisherId)) {
                continue;
            }
            UserMessage m = new UserMessage();
            m.setUserId(follower.getId());
            m.setType(TYPE_FOLLOW_PUBLISH_SET);
            m.setTitle("你关注的用户发布了新题库");
            m.setContentPreview(pv);
            m.setRefQuestionSetId(setId);
            m.setRefPaperId(null);
            m.setRefCommentId(null);
            m.setActorUserId(publisherId);
            userMessageMapper.insert(m);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyFollowersNewSharedPaper(Long ownerId, Long paperId, String title) {
        if (ownerId == null || paperId == null) {
            return;
        }
        List<User> followers = userFollowMapper.selectFollowerUsers(ownerId);
        if (followers == null || followers.isEmpty()) {
            return;
        }
        String pv = preview(title != null ? title : "试卷", 200);
        for (User follower : followers) {
            if (follower == null || follower.getId() == null || follower.getId().equals(ownerId)) {
                continue;
            }
            UserMessage m = new UserMessage();
            m.setUserId(follower.getId());
            m.setType(TYPE_FOLLOW_PUBLISH_PAPER);
            m.setTitle("你关注的用户发布了新试卷");
            m.setContentPreview(pv);
            m.setRefQuestionSetId(null);
            m.setRefPaperId(paperId);
            m.setRefCommentId(null);
            m.setActorUserId(ownerId);
            userMessageMapper.insert(m);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyDiscussionComment(Long recipientUserId, Long setId, Long commentId, Long actorUserId, String contentPreview) {
        if (recipientUserId == null || setId == null || commentId == null) {
            return;
        }
        UserMessage m = new UserMessage();
        m.setUserId(recipientUserId);
        m.setType(TYPE_DISCUSSION_COMMENT);
        m.setTitle("你的公开题库收到新评论");
        m.setContentPreview(preview(contentPreview, 240));
        m.setRefQuestionSetId(setId);
        m.setRefPaperId(null);
        m.setRefCommentId(commentId);
        m.setActorUserId(actorUserId);
        userMessageMapper.insert(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return 0;
        }
        return userMessageMapper.markRead(messageId, userId);
    }

    @Override
    public int countUnread(Long userId) {
        if (userId == null) {
            return 0;
        }
        return userMessageMapper.countUnread(userId);
    }

    @Override
    public List<UserMessage> listLatestUnread(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        int lim = Math.min(Math.max(limit, 1), 20);
        return userMessageMapper.selectLatestUnread(userId, lim);
    }

    @Override
    public Map<String, Object> listMessages(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 20;
        }
        int offset = (page - 1) * size;
        List<UserMessage> list = userMessageMapper.selectByUserIdPage(userId, offset, size);
        int total = userMessageMapper.countByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", total);
        map.put("page", page);
        map.put("size", size);
        map.put("unreadCount", userMessageMapper.countUnread(userId));
        return map;
    }
}
