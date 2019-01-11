package com.wisesupport.user;

import lombok.Data;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Data
public class User {

    private long id;

    private String account;

    private String password;

    private String locale;

}
