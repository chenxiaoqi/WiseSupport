package com.lazyman.timetennis.user;

import com.lazyman.timetennis.BaseEvent;

public class BalanceEvent extends BaseEvent {

    private int balance;

    private int fee;

    private User target;

    private int discountFee;

    public BalanceEvent(Object source, User operator, User target, int balance, int fee, int discountFee) {
        super(source, operator, BaseEvent.OP_CHARGE);
        this.balance = balance;
        this.fee = fee;
        this.target = target;
        this.discountFee = discountFee;
    }

    public int getBalance() {
        return balance;
    }

    public int getFee() {
        return fee;
    }

    public User getTarget() {
        return target;
    }

    public int getDiscountFee() {
        return discountFee;
    }
}
