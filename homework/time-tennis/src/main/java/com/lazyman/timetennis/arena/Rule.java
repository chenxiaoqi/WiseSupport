package com.lazyman.timetennis.arena;

import lombok.Data;

@Data
public class Rule {
    private int id;
    private int courtId;
    private String name;
    private String startDate;
    private String endDate;
    private Integer week;
    private Integer startHour;
    private Integer endHour;
    private Integer fee;
    private int type;
}
