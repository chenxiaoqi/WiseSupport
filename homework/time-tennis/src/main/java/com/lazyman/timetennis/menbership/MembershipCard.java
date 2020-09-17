package com.lazyman.timetennis.menbership;

import lombok.Data;

@Data
public class MembershipCard {
    private int id;
    private int balance;
    private MembershipCardMeta meta;
}
