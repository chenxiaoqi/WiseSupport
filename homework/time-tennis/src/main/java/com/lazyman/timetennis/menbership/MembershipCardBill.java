package com.lazyman.timetennis.menbership;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lazyman.timetennis.user.User;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class MembershipCardBill {
    private int id;
    private User user;
    private String productType;
    private int mcId;
    private int fee;
    private int balance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
}
