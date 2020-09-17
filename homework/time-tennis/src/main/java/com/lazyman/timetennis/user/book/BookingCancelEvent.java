package com.lazyman.timetennis.user.book;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.user.User;

public class BookingCancelEvent extends BaseBookingEvent {

    public BookingCancelEvent(Object source, User operator, Booking booking) {
        super(source, operator, booking, BaseEvent.OP_BOOK_CANCEL);
    }

}