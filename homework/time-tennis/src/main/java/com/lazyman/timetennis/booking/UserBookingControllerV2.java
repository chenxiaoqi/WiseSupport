package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.arena.*;
import com.lazyman.timetennis.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Date;
import java.util.List;

@RestController
public class UserBookingControllerV2 {

    private RuleDao ruleDao;

    private CourtDao courtDao;

    private BookingMapper bookingMapper;

    public UserBookingControllerV2(RuleDao ruleDao, CourtDao courtDao, BookingMapper bookingMapper) {
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping("/user/v2/booking")
    @Transactional
    public synchronized void booking(@SessionAttribute("user") User user,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                     int arenaId,
                                     int[] courtIds,
                                     int[] startTimes,
                                     int totalFee) {
        //todo 有未完成支付的不让预定
        int tf = 0;
        for (int i = 0; i < courtIds.length; i++) {
            int courtId = courtIds[i];
            int startTime = startTimes[i];
            List<Rule> rules = ruleDao.courtRules(new Object[]{courtId});
            if (!BookingTool.isBookable(rules, date, startTime, startTime + 1)) {
                throw new BusinessException("时间段不可预定");
            }
            int fee = BookingTool.calcFeeV2(rules, date, startTime, courtDao, courtId);
            Booking booking = new Booking();
            Arena arena = new Arena();
            arena.setId(arenaId);
            booking.setArena(arena);
            Court court = new Court();
            court.setId(courtId);
            booking.setCourt(court);
            booking.setDate(date);
            booking.setStart(startTime);
            booking.setEnd(startTime + 1);
            booking.setOpenId(user.getOpenId());
            booking.setFee(fee);
            bookingMapper.insert(booking);
            tf = tf + fee;
        }
        if (tf != totalFee) {
            throw new BusinessException("对不起,总费用不匹配,请联系管理员处理!");
        }
    }

    @GetMapping("/mine/bookings")
    public List<Booking> bookings(String openId) {
        return bookingMapper.userBookings(openId);
    }

}
