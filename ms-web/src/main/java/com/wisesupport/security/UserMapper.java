package com.wisesupport.security;

import org.apache.ibatis.annotations.Mapper;

/**
 * Author chenxiaoqi on 2018/12/26.
 */
@Mapper
public interface UserMapper {

    void delete(long id);

}
