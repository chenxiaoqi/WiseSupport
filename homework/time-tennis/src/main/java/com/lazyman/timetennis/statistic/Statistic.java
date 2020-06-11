package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.user.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Statistic {
    private User user;
    private int fee;
    private int bookTimes;
    private int hours;
    private Date month;
    private List<Booking> bookings = new ArrayList<>();
}
