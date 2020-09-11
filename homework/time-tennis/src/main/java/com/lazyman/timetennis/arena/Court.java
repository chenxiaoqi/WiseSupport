package com.lazyman.timetennis.arena;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Court {
    private int id;
    private String name;
    private int fee;
    private List<Rule> feeRules = new ArrayList<>();
    private List<Rule> disableRules = new ArrayList<>();
    private Arena arena;

}
