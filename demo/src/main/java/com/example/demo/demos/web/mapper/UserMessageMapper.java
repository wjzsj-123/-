package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMessageMapper {

    int insert(UserMessage message);

    List<UserMessage> selectByUserIdPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    int countByUserId(@Param("userId") Long userId);

    int countUnread(@Param("userId") Long userId);

    List<UserMessage> selectLatestUnread(@Param("userId") Long userId, @Param("limit") int limit);

    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    UserMessage selectById(@Param("id") Long id);
}
