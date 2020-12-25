package com.lazyman.timetennis.wp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Trade {
    private String tradeNo;

    private String receiverId;
    private Integer receiverType;
    private String shareStatus;

    private String status;

    private String prepareId;

    private String transactionId;

    private int fee;

    private String openId;

    private String productType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
}
