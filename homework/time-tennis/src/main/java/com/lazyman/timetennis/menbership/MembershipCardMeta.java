package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.arena.Arena;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class MembershipCardMeta {
    private int id;
    private String name;
    private int initialBalance;
    private int discount;
    private int price;
    private int extendMonth;
    private String status;
    private List<Arena> arenas = Collections.emptyList();
}
