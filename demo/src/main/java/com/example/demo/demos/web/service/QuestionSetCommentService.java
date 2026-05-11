package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.QuestionSetComment;

import java.util.Map;

public interface QuestionSetCommentService {
    Long addComment(QuestionSetComment comment);

    Map<String, Object> getPublicComments(Long questionSetId, String sortBy, Long currentUserId, Integer page, Integer size);

    boolean toggleLike(Long commentId, Long questionSetId, Long userId);

    void deleteComment(Long commentId, Long questionSetId, Long operatorUserId);
}
