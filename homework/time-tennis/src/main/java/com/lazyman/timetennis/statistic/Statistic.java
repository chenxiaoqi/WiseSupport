package com.lazyman.timetennis.statistic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lazyman.timetennis.user.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Statistic {
    private String openId;
    private User user;
    private int fee;
    private int bookTimes;
    private int hours;
    @JsonFormat(pattern = "yyyy-MM")
    private Date month;
    private List<BookingBill> bills = new ArrayList<>();
}
