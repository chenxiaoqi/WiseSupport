package com.wisesupport.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * Author chenxiaoqi on 2018/12/26.
 */
@Mapper
public interface UserMapper {

    void deleteById(long id);

    void insert(User user);

    Optional<User> findByAccount(@Param("account") String account);

    List<User> findAll();

    Optional<User> findById(long id);

    void update(User user);
}
