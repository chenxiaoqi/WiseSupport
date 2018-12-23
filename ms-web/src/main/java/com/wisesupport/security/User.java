package com.wisesupport.security;

import lombok.*;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
public class User {

    @Id
    private final Long id;

    private String name;

    @AccessType(AccessType.Type.PROPERTY)
    private String password;

    private String locale;

    static User of(String name, String password, String locale) {
        return new User(null, name, password, locale);
    }

    public User withId(Long id) {
        return new User(id, name, this.password, this.locale);
    }
}
