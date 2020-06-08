package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@Validated
public class BookingController {

    private BookingMapper bookingMapper;

    public BookingController(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    @GetMapping("/recentBookings")
    public List<Booking> recentBookings() {
        Date start = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Date end = DateUtils.addDays(start, 13);
        List<Booking> result = bookingMapper.queryInRange(start, end);
        result.forEach(item -> {
            item.setCancelAble(BookingTool.cancelAble(item));
        });
        return result;
    }
}
