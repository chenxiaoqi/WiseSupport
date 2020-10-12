package com.lazyman.timetennis.booking;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class BookingSimple {
    private int courtId;

    private int start;

    private int end;

    private Date date;

    BookingSimple(Date date, int courtId, int start) {
        this.date = date;
        this.courtId = courtId;
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getCourtId() {
        return courtId;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public Date getDate() {
        return date;
    }
}
