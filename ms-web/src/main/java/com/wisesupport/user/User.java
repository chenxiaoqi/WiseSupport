package com.wisesupport.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class User implements Serializable {

   private static final long serialVersionUID = 7724710600934387088L;

    private Integer id;

    @NonNull
    private String account;

    private String password;

    private String locale;

}
