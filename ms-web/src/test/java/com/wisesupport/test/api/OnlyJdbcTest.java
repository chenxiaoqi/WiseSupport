package com.wisesupport.test.api;

import com.wisesupport.user.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Author chenxiaoqi on 2019-03-02.
 */
@JdbcTest
@Sql("/table.sql")
@RunWith(SpringRunner.class)
public class OnlyJdbcTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test() {
        userMapper.findAll();
    }

}
