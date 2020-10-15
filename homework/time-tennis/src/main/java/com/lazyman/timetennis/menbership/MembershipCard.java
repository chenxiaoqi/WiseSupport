package com.lazyman.timetennis.menbership;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lazyman.timetennis.user.User;
import lombok.Data;

import java.util.Date;

@Data
public class MembershipCard {
    private String code;
    private String openId;
    private int balance;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private MembershipCardMeta meta;
    private User user;
}
