package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    int insert(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    int delete(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    int exists(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    int countFollowing(@Param("userId") Long userId);

    int countFollowers(@Param("userId") Long userId);

    List<User> selectFollowingUsers(@Param("userId") Long userId);

    List<User> selectFollowerUsers(@Param("userId") Long userId);
}
