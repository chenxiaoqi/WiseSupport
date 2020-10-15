package com.lazyman.timetennis.arena;


import com.lazyman.timetennis.menbership.MembershipCardMeta;
import lombok.Data;

import java.util.List;

@Data
public class Arena {
    private int id;
    private String name;
    private Integer type;
    private String address;
    private String province;
    private String city;
    private String district;
    private String phone;
    private String introduction;
    private Integer advanceBookDays;
    private Integer bookStartHour;
    private Integer bookEndHour;
    private Integer bookStyle;
    private List<Court> courts;
    private String[] images;
    private String status;
    private String mchId;
    private Boolean allowHalfHour;
    private Integer bookAtLeast;
    private Integer refundTimesLimit;
    private Integer refundAdvanceHours;
    private List<MembershipCardMeta> metas;
}
