package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/booking")
    public void booking(@SessionAttribute("user") User user,
                        @Min(0) @Max(13) int dateIndex,
                        @Min(0) @Max(47) int timeIndexStart,
                        @Min(0) @Max(47) int timeIndexEnd) {
        Validate.isTrue(timeIndexEnd - timeIndexStart >= 1, "至少预定一个小时");

        Date bookingDate = DateUtils.addDays(new Date(System.currentTimeMillis()), dateIndex);
        bookingDate = DateUtils.truncate(bookingDate, Calendar.DAY_OF_MONTH);
        List<Booking> bookings = bookingMapper.queryByDate(bookingDate);
        for (Booking booking : bookings) {
            if (!(timeIndexStart < booking.getStart() && timeIndexEnd < booking.getStart() || timeIndexStart > booking.getEnd() && timeIndexEnd > booking.getEnd())) {
                throw new BusinessException("订场时间重合");
            }
        }
        Booking booking = new Booking();
        booking.setDate(bookingDate);
        booking.setStart(timeIndexStart);
        booking.setEnd(timeIndexEnd);
        booking.setOpenId(user.getOpenId());
        bookingMapper.insert(booking);
    }

    @GetMapping("/recentBookings")
    public List<Booking> recentBookings() {
        Date start = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Date end = DateUtils.addDays(start, 13);
        return bookingMapper.queryInRange(start, end);
    }
}
