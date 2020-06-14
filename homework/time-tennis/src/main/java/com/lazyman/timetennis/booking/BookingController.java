package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

    @GetMapping("/bookings")
    public List<Booking> bookings(String openId, Integer id) {
        List<Booking> result = bookingMapper.page(openId,id);
        result.forEach((item) -> {
            item.setCancelAble(BookingTool.cancelAble(item));
        });
        return result;
    }

    @DeleteMapping("/booking/{id}")
    @Transactional
    public void deleteBooking(@SessionAttribute("user") User user, @PathVariable int id) {
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }
}
