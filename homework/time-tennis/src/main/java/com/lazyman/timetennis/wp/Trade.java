package com.lazyman.timetennis.wp;

import lombok.Data;

@Data
public class Trade {
    private String tradeNo;

    private String mchId;

    private String status;

    private String prepareId;

    private int fee;

    private String openId;

    private String productType;
}
