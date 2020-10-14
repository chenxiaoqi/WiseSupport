package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.arena.Arena;
import lombok.Data;

@Data
public class MembershipCardMeta {
    private int id;
    private String name;
    private int initialBalance;
    private int discount;
    private int price;
    private int extendMonth;
    private String status;
    private Arena arena;
}
