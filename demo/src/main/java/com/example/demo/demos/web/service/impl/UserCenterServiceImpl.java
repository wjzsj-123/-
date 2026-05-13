package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.mapper.QuestionSetMapper;
import com.example.demo.demos.web.mapper.UserFollowMapper;
import com.example.demo.demos.web.mapper.UserMapper;
import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.pojo.UserCenterDTO;
import com.example.demo.demos.web.service.UserCenterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserCenterServiceImpl implements UserCenterService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private QuestionSetMapper questionSetMapper;
    @Resource
    private PaperMapper paperMapper;

    @Override
    public UserCenterDTO getUserCenter(Long userId, Long viewerId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        boolean own = viewerId != null && viewerId.equals(userId);
        UserCenterDTO dto = new UserCenterDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setOwnProfile(own);
        dto.setEmail(own ? user.getEmail() : null);
        dto.setFollowingCount(userFollowMapper.countFollowing(userId));
        dto.setFollowerCount(userFollowMapper.countFollowers(userId));
        dto.setPublicQuestionSets(questionSetMapper.selectPublicByPublisherId(userId));
        dto.setPublicPapers(paperMapper.selectSharedByUserId(userId));
        if (!own && viewerId != null) {
            dto.setViewerFollows(userFollowMapper.exists(viewerId, userId) > 0);
        } else {
            dto.setViewerFollows(Boolean.FALSE);
        }
        return dto;
    }

    @Override
    public List<User> listFollowing(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<User> list = userFollowMapper.selectFollowingUsers(userId);
        sanitizeUsers(list);
        return list;
    }

    @Override
    public List<User> listFollowers(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<User> list = userFollowMapper.selectFollowerUsers(userId);
        sanitizeUsers(list);
        return list;
    }

    private void sanitizeUsers(List<User> list) {
        if (list == null) {
            return;
        }
        for (User u : list) {
            if (u == null) {
                continue;
            }
            u.setPassword(null);
            u.setEmail(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("不能关注自己");
        }
        if (userMapper.selectById(followeeId) == null) {
            throw new IllegalArgumentException("被关注用户不存在");
        }
        if (userFollowMapper.exists(followerId, followeeId) > 0) {
            return;
        }
        userFollowMapper.insert(followerId, followeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        userFollowMapper.delete(followerId, followeeId);
    }
}
