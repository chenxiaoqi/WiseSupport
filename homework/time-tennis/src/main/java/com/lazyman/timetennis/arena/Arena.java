package com.lazyman.timetennis.arena;


import lombok.Data;

import java.util.List;

@Data
public class Arena {
    private int id;
    private String name;
    private int type;
    private String address;
    private String province;
    private String city;
    private String district;
    private String phone;
    private String introduction;
    private int advanceBookDays;
    private int bookStartHour;
    private int bookEndHour;
    private int bookStyle = 1;
    private List<Court> courts;
    private String[] images;
    private String status;
    private String mchId;
}
