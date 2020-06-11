package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.*;

@Component
@Slf4j
public class MonthlyStatisticTask {
    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    private BookingMapper mapper;

    private JdbcTemplate template;

    public MonthlyStatisticTask(BookingMapper mapper, JdbcTemplate template) {
        this.mapper = mapper;
        this.template = template;
    }

//    @Scheduled(fixedDelay = 3600000)
    @Scheduled(cron = "0 0 1 1 1/1 ? ")
    @Transactional
    public void run() throws ParseException {
//        Date end = DateUtils.truncate(new Date(), Calendar.MONTH);
        Date end = DateUtils.truncate(FORMAT.parse("2020-07-11"), Calendar.MONTH);
        Date start = DateUtils.addMonths(end, -1);

        log.info("monthly statistic task start {}", FORMAT.format(start));
        List<Booking> bookings = mapper.query(null, start, end);
        Collection<Statistic> statistics = calc(bookings, start);
        for (Statistic statistic : statistics) {
            template.update("insert into monthly_stat (month ,open_id,fee,hours,book_times)values(?,?,?,?,?)",
                    statistic.getMonth(),
                    statistic.getUser().getOpenId(),
                    statistic.getFee(),
                    statistic.getHours(),
                    statistic.getBookTimes());
        }
        for (Booking booking : bookings) {
            template.update("update tt_booking set charged= 1 where id=?", booking.getId());
        }
    }

    private Collection<Statistic> calc(List<Booking> bookings, Date start) {
        Map<String, Statistic> mapping = new HashMap<>();
        for (Booking booking : bookings) {
            Statistic statistic = putIfAbsent(mapping, booking.getOwner(), start);
            statistic.setBookTimes(statistic.getBookTimes() + 1);
            statistic.setHours(statistic.getHours() + booking.getEnd() - booking.getStart() + 1);
            statistic.getBookings().add(booking);
            if (CollectionUtils.isEmpty(booking.getShareUsers())) {
                statistic.setFee(statistic.getFee() + booking.getFee());
            } else {
                int average = booking.getFee() / (booking.getShareUsers().size() + 1);
                statistic.setFee(statistic.getFee() + booking.getFee() - average * booking.getShareUsers().size());
                for (User user : booking.getShareUsers()) {
                    Statistic ss = putIfAbsent(mapping, user, start);
                    ss.setFee(ss.getFee() + average);
                    ss.setHours(ss.getHours() + booking.getEnd() - booking.getStart() + 1);
                    ss.setBookTimes(ss.getBookTimes() + 1);
                    ss.getBookings().add(booking);
                }
            }
        }
        return mapping.values();
    }

    private static Statistic putIfAbsent(Map<String, Statistic> map, User user, Date start) {
        return map.computeIfAbsent(user.getOpenId(), k -> {
            Statistic s = new Statistic();
            s.setUser(user);
            s.setMonth(start);
            return s;
        });
    }
}
