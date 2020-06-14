package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.user.User;

public class MonthFeeEvent extends BaseEvent {

    public MonthFeeEvent(Object source, User operator) {
        super(source, operator, BaseEvent.OP_MDC);
    }
}
