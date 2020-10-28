package com.lazyman.timetennis.booking;

import lombok.Data;

import java.util.Date;

@Data
public class QueryParam {

    private Date start;
    private Date end;
    private String payType;
    private String status;
    private Boolean charged;
    private Integer arenaId;
    private String payNo;
}
