package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.*;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserBookingController implements ApplicationContextAware {

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    private BookingMapper bookingMapper;

    private ApplicationContext application;

    private int cancelTimeLimit;

    private JdbcTemplate template;

    private int defaultArenaId;

    private int defaultCourtId;

    private RuleDao ruleDao;

    private CourtDao courtDao;

    public UserBookingController(BookingMapper bookingMapper,
                                 @Value("${wx.cancel-times-limit}") int cancelTimeLimit,
                                 @Value("${wx.default-arena-id}") int defaultArenaId,
                                 @Value("${wx.default-court-id}") int defaultCourtId,
                                 JdbcTemplate template, RuleDao ruleDao, CourtDao courtDao) {
        this.bookingMapper = bookingMapper;
        this.cancelTimeLimit = cancelTimeLimit;
        this.template = template;
        this.defaultArenaId = defaultArenaId;
        this.defaultCourtId = defaultCourtId;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
    }

    @PostMapping("/booking")
    @Transactional
    public synchronized void booking(User user,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                     @Min(0) @Max(47) int timeIndexStart,
                                     @Min(0) @Max(47) int timeIndexEnd) {
        if (!user.getVip()) {
            throw new BusinessException("您还不是会员,请联系管理员授权");
        }

        Validate.isTrue(timeIndexEnd - timeIndexStart >= 1, "至少预定一个小时");

        Calendar now = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
        if (date.getTime() < now.getTimeInMillis()) {
            throw new BusinessException("预定日期错误: " + FORMAT.format(date));
        }

        int nowTimeIndex = now.get(Calendar.HOUR_OF_DAY) * 2;
        List<Rule> rules = ruleDao.courtRules(new Object[]{defaultCourtId});
        if (!BookingTool.isBookable(rules, date, timeIndexStart, timeIndexEnd)) {
            throw new BusinessException("对不起,该时间段已被预定");
        }

        if (!user.getAdmin()) {
            int count = bookingMapper.countBooked(user.getOpenId(), DateUtils.truncate(now.getTime(), Calendar.DAY_OF_MONTH), nowTimeIndex);
            if (count + timeIndexEnd - timeIndexStart > 18) {
                throw new BusinessException("累计预定不能超过9个小时,请照顾一下其他会员哦!");
            }
        }

        if (user.getBalance() <= 0) {
            throw new BusinessException("账户余额不足");
        }

        List<Booking> bookings = bookingMapper.queryByDate(date, defaultArenaId);
        for (Booking booking : bookings) {
            if (!(timeIndexStart < booking.getStart() && timeIndexEnd < booking.getStart() || timeIndexStart > booking.getEnd() && timeIndexEnd > booking.getEnd())) {
                throw new BusinessException("订场时间重合");
            }
        }
        Booking booking = new Booking();
        Arena arena = new Arena();
        arena.setId(defaultArenaId);
        booking.setArena(arena);
        Court court = new Court();
        court.setId(defaultCourtId);
        booking.setCourt(court);
        booking.setDate(date);
        booking.setStart(timeIndexStart);
        booking.setEnd(timeIndexEnd);
        booking.setOpenId(user.getOpenId());
        booking.setFee(BookingTool.calcFee(rules, date, timeIndexStart, timeIndexEnd, courtDao, defaultCourtId));
        bookingMapper.insert(booking);
        bookingMapper.deleteShare(booking.getId());
        application.publishEvent(new BookingEvent(this, user, booking));
    }

    @DeleteMapping("/booking/{id}")
    @Transactional
    public void cancelBooking(User user, @PathVariable int id) {
        Booking dbBooking = bookingMapper.selectByPrimaryKey(id);
        Validate.notNull(dbBooking, "订场信息不存在");

        if (!dbBooking.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地不是你定的哦");
        }

        if (!BookingTool.cancelAble(dbBooking)) {
            throw new BusinessException("预定场地必须提前两个小时取消,如有特殊需求请联系管理员");
        }

        checkCancelTimesLimit(user);

        Booking booking = new Booking();
        booking.setOpenId(user.getOpenId());
        booking.setId(id);
        bookingMapper.deleteBooking(booking);
        bookingMapper.deleteShare(id);
        application.publishEvent(new BookingCancelEvent(this, user, dbBooking));
    }

    private void checkCancelTimesLimit(User user) {
        Date start = DateUtils.truncate(new Date(), Calendar.MONTH);
        Date end = DateUtils.addMonths(start, 1);
        Integer count = template.queryForObject("select count(1) from operation where operator_id=?  and operation_type='cb' and update_time>=? and update_time<?",
                Integer.class, user.getOpenId(), start, end);
        Validate.notNull(count);
        if (count >= cancelTimeLimit) {
            throw new BusinessException("您本月取消次数已用完,如特殊需求请联系管理员!");
        }

    }

    @PostMapping("/share/booking/{bookingId}")
    public void shareBooking(User user, @PathVariable int bookingId) {
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        Validate.notNull(booking);

        if (!user.getVip()) {
            throw new BusinessException("您还不是会员,请联系管理员授权");
        }
        if (booking.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地就是你定的哦");
        }

        if (booking.getCharged()) {
            throw new BusinessException("已出账单,不能分摊了哦");
        }
        try {
            bookingMapper.addShare(booking.getId(), user.getOpenId());
        } catch (DuplicateKeyException e) {
            throw new BusinessException("您已经分摊过了");
        }
        application.publishEvent(new BookingShareEvent(this, user, booking));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }
}
