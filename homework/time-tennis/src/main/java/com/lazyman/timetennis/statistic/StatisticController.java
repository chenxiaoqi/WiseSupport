package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.booking.BookingMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
public class StatisticController {

    private BookingMapper mapper;

    public StatisticController(BookingMapper mapper) {
        this.mapper = mapper;
    }

    public void userMonth(@DateTimeFormat(pattern = "yyyy-MM-dd") Date month) {
        if (month == null) {
            month = new Date();
        }
        Date start = DateUtils.truncate(month, Calendar.MONTH);
        Date end = DateUtils.addMonths(start, 1);
    }
}
