package com.lazyman.timetennis.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class OrderDetail {
    private String payType;

    private String payNo;

    private String transactionId;

    private String code;

    private int fee;

    private String mcId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    private List<Booking> bookings;
}
