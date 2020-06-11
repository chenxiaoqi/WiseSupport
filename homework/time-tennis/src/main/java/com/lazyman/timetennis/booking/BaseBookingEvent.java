package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.user.User;

public class BaseBookingEvent extends BaseEvent {

    private Booking booking;

    private String operationType;

    public BaseBookingEvent(Object source, User operator, Booking booking,String operationType) {
        super(source, operator);
        this.booking = booking;
        this.operationType = operationType;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getOperationType() {
        return operationType;
    }
}
