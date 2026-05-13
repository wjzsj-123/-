package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.UserMessage;

import java.util.List;
import java.util.Map;

public interface UserMessageService {

    void notifyFollowersNewPublicQuestionSet(Long publisherId, Long setId, String setName);

    void notifyFollowersNewSharedPaper(Long ownerId, Long paperId, String title);

    void notifyDiscussionComment(Long recipientUserId, Long setId, Long commentId, Long actorUserId, String contentPreview);

    int markRead(Long messageId, Long userId);

    int countUnread(Long userId);

    List<UserMessage> listLatestUnread(Long userId, int limit);

    Map<String, Object> listMessages(Long userId, int page, int size);
}
