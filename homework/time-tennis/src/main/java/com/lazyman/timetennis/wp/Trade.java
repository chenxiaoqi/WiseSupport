package com.lazyman.timetennis.wp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Trade {
    private String tradeNo;

    private String mchId;

    private String status;

    private String prepareId;

    private int fee;

    private String openId;

    private String productType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
}
