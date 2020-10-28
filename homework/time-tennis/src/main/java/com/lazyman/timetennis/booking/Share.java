package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.user.User;

public class Share extends User {
    private String payNo;

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }
}
