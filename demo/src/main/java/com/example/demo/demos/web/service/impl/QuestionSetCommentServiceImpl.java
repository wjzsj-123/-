package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.QuestionSetCommentLikeMapper;
import com.example.demo.demos.web.mapper.QuestionSetCommentMapper;
import com.example.demo.demos.web.mapper.QuestionSetMapper;
import com.example.demo.demos.web.pojo.QuestionSet;
import com.example.demo.demos.web.pojo.QuestionSetComment;
import com.example.demo.demos.web.pojo.QuestionSetCommentLike;
import com.example.demo.demos.web.service.QuestionSetCommentService;
import com.example.demo.demos.web.service.UserMessageService;
import com.example.demo.demos.web.redis.ActionRateLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionSetCommentServiceImpl implements QuestionSetCommentService {
    @Resource
    private QuestionSetCommentMapper questionSetCommentMapper;

    @Resource
    private QuestionSetCommentLikeMapper questionSetCommentLikeMapper;

    @Resource
    private QuestionSetMapper questionSetMapper;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private ActionRateLimitService actionRateLimitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(QuestionSetComment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("评论数据不能为空");
        }
        if (comment.getQuestionSetId() == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        if (comment.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (comment.getContent().length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字");
        }
        if (comment.getSentiment() == null || (comment.getSentiment() != 1 && comment.getSentiment() != -1)) {
            throw new IllegalArgumentException("评价类型无效");
        }
        if (!actionRateLimitService.allowComment(comment.getUserId())) {
            throw new IllegalArgumentException("评论过于频繁，请稍后再试");
        }

        QuestionSet set = questionSetMapper.selectById(comment.getQuestionSetId());
        if (set == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        if (set.getIsPublic() == null || set.getIsPublic() != 1) {
            throw new IllegalArgumentException("该题库当前非公开状态，无法留言");
        }

        comment.setContent(comment.getContent().trim());
        questionSetCommentMapper.insert(comment);

        Long recipient = set.getPublisherId() != null ? set.getPublisherId() : set.getUserId();
        if (recipient != null && !recipient.equals(comment.getUserId())) {
            userMessageService.notifyDiscussionComment(
                    recipient,
                    set.getId(),
                    comment.getId(),
                    comment.getUserId(),
                    comment.getContent()
            );
        }
        return comment.getId();
    }

    @Override
    public Map<String, Object> getPublicComments(Long questionSetId, String sortBy, Long currentUserId, Integer page, Integer size) {
        if (questionSetId == null) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        if (!"hot".equals(sortBy)) {
            sortBy = "latest";
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<QuestionSetComment> list = questionSetCommentMapper.selectPublicComments(questionSetId, sortBy, currentUserId, offset, size);
        int total = questionSetCommentMapper.countPublicComments(questionSetId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long commentId, Long questionSetId, Long userId) {
        if (commentId == null || questionSetId == null || userId == null) {
            throw new IllegalArgumentException("评论ID、题库ID、用户ID不能为空");
        }
        QuestionSet set = questionSetMapper.selectById(questionSetId);
        if (set == null || set.getIsPublic() == null || set.getIsPublic() != 1) {
            throw new IllegalArgumentException("该题库当前非公开状态，无法点赞");
        }

        QuestionSetComment comment = questionSetCommentMapper.selectById(commentId);
        if (comment == null || !questionSetId.equals(comment.getQuestionSetId())) {
            throw new IllegalArgumentException("评论不存在");
        }

        QuestionSetCommentLike existing = questionSetCommentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        if (existing == null) {
            QuestionSetCommentLike like = new QuestionSetCommentLike();
            like.setCommentId(commentId);
            like.setQuestionSetId(questionSetId);
            like.setUserId(userId);
            questionSetCommentLikeMapper.insert(like);
            questionSetCommentMapper.incrementLikeCount(commentId);
            return true;
        } else {
            questionSetCommentLikeMapper.deleteByCommentIdAndUserId(commentId, userId);
            questionSetCommentMapper.decrementLikeCount(commentId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long questionSetId, Long operatorUserId) {
        if (commentId == null || questionSetId == null || operatorUserId == null) {
            throw new IllegalArgumentException("评论ID、题库ID、操作用户ID不能为空");
        }
        QuestionSetComment comment = questionSetCommentMapper.selectById(commentId);
        if (comment == null || !questionSetId.equals(comment.getQuestionSetId())) {
            throw new IllegalArgumentException("评论不存在");
        }

        QuestionSet set = questionSetMapper.selectById(questionSetId);
        if (set == null) {
            throw new IllegalArgumentException("题库不存在");
        }

        boolean isCommentOwner = operatorUserId.equals(comment.getUserId());
        boolean isSetOwner = operatorUserId.equals(set.getUserId());
        if (!isCommentOwner && !isSetOwner) {
            throw new IllegalArgumentException("仅评论作者或题库拥有者可删除评论");
        }

        questionSetCommentLikeMapper.deleteByCommentId(commentId);
        questionSetCommentMapper.deleteById(commentId);
    }
}
