package com.lazyman.timetennis.arena;

import lombok.Data;

@Data
public class Rule {
    private Integer id;
    private Integer courtId;
    private Integer arenaId;
    private String name;
    private String startDate;
    private String endDate;
    private Integer week;
    private Integer startHour;
    private Integer endHour;
    private Integer fee;
    private int type;
}
