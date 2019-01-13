package com.wisesupport.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Author chenxiaoqi on 2018/12/26.
 */
@Mapper
public interface UserMapper {

    void deleteById(int id);

    void insert(User user);

    User findByAccount(@Param("account") String account);

    List<User> findAll();

    User findById(int id);

    void update(User user);
}
