package com.example.demo.demos.web.mapper;

import com.example.demo.demos.web.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    // 新增用户
    int insert(User user);

    // 根据ID删除用户
    int deleteById(Long id);

    // 更新用户信息
    int updateById(User user);

    // 根据ID查询用户
    User selectById(Long id);

    // 根据用户名查询用户（用于登录）
    User selectByUsername(String username);

    // 查询所有用户（分页场景可添加参数）
    List<User> selectAll();
}