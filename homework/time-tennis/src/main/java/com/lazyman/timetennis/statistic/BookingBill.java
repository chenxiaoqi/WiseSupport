package com.lazyman.timetennis.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookingBill {
    private String openId;
    private int bookingId;
    private int fee;
    private boolean share;
    private Date date;
    private int start;
    private int end;


}
