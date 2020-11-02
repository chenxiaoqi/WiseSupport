package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@Validated
public class BookingController {

    private BookingMapper bookingMapper;

    private ArenaDao arenaDao;

    private BookSchedulerRepository repository;

    public BookingController(BookingMapper bookingMapper,
                             ArenaDao arenaDao,
                             BookSchedulerRepository repository) {
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
        this.repository = repository;
    }

    @GetMapping("/recentBookings")
    public List<Booking> recentBookings(Integer arenaId, @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Date start;
        Date end;
        Arena arena = arenaDao.load(arenaId);
        if (date == null) {
            start = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
            end = DateUtils.addDays(start, arena.getAdvanceBookDays() - 1);
        } else {
            start = date;
            end = date;
        }

        List<Booking> result = bookingMapper.queryInRange(arenaId, start, end);
        result.forEach(item -> item.setCancelAble(BookingTool.cancelAble(item, arena.getRefundAdvanceHours())));
        return result;
    }

    @GetMapping("/v2/recentBookings")
    public List<BookingSimple> recentBookingsV2(@RequestParam Integer arenaId,
                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        BookScheduler scheduler = repository.arenaScheduler(arenaId);
        return scheduler.getBookings(date);
    }
}
