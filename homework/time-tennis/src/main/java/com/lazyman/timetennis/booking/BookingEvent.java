package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.user.User;

public class BookingEvent extends BaseBookingEvent {


    public BookingEvent(Object source, User operator, Booking booking) {
        super(source,operator,booking, BaseEvent.OP_BOOK);
    }

}
