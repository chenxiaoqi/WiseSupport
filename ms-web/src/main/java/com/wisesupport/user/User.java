package com.wisesupport.user;

import lombok.Data;
import java.io.Serializable;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Data
public class User implements Serializable {

   private static final long serialVersionUID = 7724710600934387088L;

    private Integer id;

    private String account;

    private String password;

    private String locale;

}
