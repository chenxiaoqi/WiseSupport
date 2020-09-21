package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BaseEvent;
import com.lazyman.timetennis.user.User;

class BookingCancelEvent extends BaseBookingEvent {

    BookingCancelEvent(Object source, User operator, Booking booking) {
        super(source, operator, booking, BaseEvent.OP_BOOK_CANCEL);
    }

}
