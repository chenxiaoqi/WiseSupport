package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.user.User;

public class BaseBookingEvent extends BaseEvent {

    private Booking booking;

    public BaseBookingEvent(Object source, User operator, Booking booking,String operationType) {
        super(source, operator, operationType);
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }

}
