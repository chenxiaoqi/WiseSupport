package com.wisesupport.security;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
public interface UserRepository extends CrudRepository<User, Long> {

    @Query("select id,name,password,locale from user where name=:name")
    Optional<User> findByName(@Param("name") String name);
}
