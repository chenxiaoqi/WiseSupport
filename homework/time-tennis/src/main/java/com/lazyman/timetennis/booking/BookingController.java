package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@Validated
public class BookingController implements ApplicationContextAware {

    private BookingMapper bookingMapper;

    private ApplicationContext application;

    private int defaultArenaId;

    private ArenaDao arenaDao;

    private BookSchedulerRepository repository;

    public BookingController(BookingMapper bookingMapper, @Value("${wx.default-arena-id}") int defaultArenaId, ArenaDao arenaDao, BookSchedulerRepository repository) {
        this.bookingMapper = bookingMapper;
        this.defaultArenaId = defaultArenaId;
        this.arenaDao = arenaDao;
        this.repository = repository;
    }

    @GetMapping("/recentBookings")
    public List<Booking> recentBookings(Integer arenaId, @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Date start;
        Date end;
        if (arenaId == null) {
            arenaId = defaultArenaId;
        }
        if (date == null) {
            Arena arena = arenaDao.load(arenaId);
            start = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
            end = DateUtils.addDays(start, arena.getAdvanceBookDays() - 1);
        } else {
            start = date;
            end = date;
        }

        List<Booking> result = bookingMapper.queryInRange(arenaId, start, end);
        result.forEach(item -> item.setCancelAble(BookingTool.cancelAble(item)));
        return result;
    }

    @GetMapping("/v2/recentBookings")
    public List<BookingSimple> recentBookingsV2(@RequestParam Integer arenaId,
                                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        BookScheduler scheduler = repository.arenaScheduler(arenaId);
        return scheduler.getBookings(date);
    }


    @GetMapping("/bookings")
    public List<Booking> bookings(String openId, Integer id) {
        List<Booking> result = bookingMapper.page(openId, id);
        result.forEach((item) -> item.setCancelAble(BookingTool.cancelAble(item)));
        return result;
    }

    @DeleteMapping("/booking/{id}")
    @Transactional
    public void deleteBooking(User user, @PathVariable int id) {
        if (!user.getAdmin()) {
            throw new BusinessException("此为管理员功能,普通用户可以到首页里取消");
        }

        Booking dbBooking = bookingMapper.selectByPrimaryKey(id);
        Validate.notNull(dbBooking, "订场信息不存在");

        if (dbBooking.getCharged()) {
            throw new BusinessException("已出账单,不能删除");
        }

        Booking booking = new Booking();
        booking.setId(id);
        bookingMapper.deleteBooking(booking);
        bookingMapper.deleteShare(id);

        application.publishEvent(new BookingCancelEvent(this, user, dbBooking));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }
}
