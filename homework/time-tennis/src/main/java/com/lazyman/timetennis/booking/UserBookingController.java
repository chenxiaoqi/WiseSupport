package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserBookingController {

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    private BookingMapper bookingMapper;

    public UserBookingController(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    @PostMapping("/booking")
    @Transactional
    public void booking(@SessionAttribute("user") User user,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                        @Min(0) @Max(47) int timeIndexStart,
                        @Min(0) @Max(47) int timeIndexEnd) {
        if (!user.getVip()) {
            throw new BusinessException("您还不是会员,请联系管理员授权");
        }

        Validate.isTrue(timeIndexEnd - timeIndexStart >= 1, "至少预定一个小时");

        Calendar now = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
        if (date.getTime() < now.getTimeInMillis()) {
            throw new BusinessException("预定日志错误: " + FORMAT.format(date));
        }

        int nowTimeIndex = now.get(Calendar.HOUR_OF_DAY) * 2;
        int count = bookingMapper.countBooked(user.getOpenId(), DateUtils.truncate(now.getTime(), Calendar.DAY_OF_MONTH), nowTimeIndex);
        if (count + timeIndexEnd - timeIndexStart > 18) {
            throw new BusinessException("累计预定不能超过9个小时,请照顾一下其他会员哦!");
        }

        List<Booking> bookings = bookingMapper.queryByDate(date);
        for (Booking booking : bookings) {
            if (!(timeIndexStart < booking.getStart() && timeIndexEnd < booking.getStart() || timeIndexStart > booking.getEnd() && timeIndexEnd > booking.getEnd())) {
                throw new BusinessException("订场时间重合");
            }
        }
        Booking booking = new Booking();
        booking.setDate(date);
        booking.setStart(timeIndexStart);
        booking.setEnd(timeIndexEnd);
        booking.setOpenId(user.getOpenId());
        booking.setFee(BookingTool.calcFee(date, timeIndexStart, timeIndexEnd));
        bookingMapper.insert(booking);

        bookingMapper.deleteShare(booking.getId());
    }

    @DeleteMapping("/booking/{id}")
    @Transactional
    public void cancelBooking(@SessionAttribute User user, @PathVariable int id) {
        Booking dbBooking = bookingMapper.selectByPrimaryKey(id);
        Validate.notNull(dbBooking, "订场信息不存在");

        if (!dbBooking.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地不是你定的哦");
        }

        if (!BookingTool.cancelAble(dbBooking)) {
            throw new BusinessException("预定场地必须提前两个小时取消,如有特殊需求请联系管理员");
        }


        Booking booking = new Booking();
        booking.setOpenId(user.getOpenId());
        booking.setId(id);
        bookingMapper.deleteBooking(booking);
        bookingMapper.deleteShare(id);
    }

    @PostMapping("/share/booking/{bookingId}")
    public void shareBooking(@SessionAttribute("user") User user,@PathVariable int bookingId) {
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        Validate.notNull(booking);

        if (booking.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地就是你定的哦");
        }

        if (booking.getCharged()) {
            throw new BusinessException("已出账单,不能分摊了哦");
        }
        try {
            bookingMapper.addShare(booking.getId(), user.getOpenId());
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已经分摊过了");
        }

    }
}
