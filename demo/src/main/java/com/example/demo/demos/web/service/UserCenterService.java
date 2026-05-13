package com.example.demo.demos.web.service;

import com.example.demo.demos.web.pojo.User;
import com.example.demo.demos.web.pojo.UserCenterDTO;

import java.util.List;

public interface UserCenterService {

    UserCenterDTO getUserCenter(Long userId, Long viewerId);

    List<User> listFollowing(Long userId);

    List<User> listFollowers(Long userId);

    void follow(Long followerId, Long followeeId);

    void unfollow(Long followerId, Long followeeId);
}
