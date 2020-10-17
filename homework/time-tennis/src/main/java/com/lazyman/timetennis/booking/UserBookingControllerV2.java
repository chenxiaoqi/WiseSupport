package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.core.LockRepository;
import com.lazyman.timetennis.menbership.*;
import com.lazyman.timetennis.statistic.StatisticDao;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.BasePayController;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserBookingControllerV2 extends BasePayController implements ApplicationListener<TradeEvent>, ApplicationContextAware {
    private MembershipCardBillDao billDao;

    private BookingMapper bookingMapper;

    private MembershipCardDao mcDao;

    private ArenaDao arenaDao;

    private BookSchedulerRepository schedulerRepository;

    private LockRepository lockRepository;

    private ApplicationContext context;

    private TransactionTemplate tt;

    private MembershipCardService cardService;

    private StatisticDao statisticDao;

    private int defaultArenaId;

    private int defaultCourtId;

    public UserBookingControllerV2(
            @Value("${wx.default-arena-id}") int defaultArenaId,
            @Value("${wx.default-court-id}") int defaultCourtId,
            MembershipCardBillDao billDao,
            BookingMapper bookingMapper,
            MembershipCardDao mcDao,
            ArenaDao arenaDao,
            BookSchedulerRepository schedulerRepository,
            LockRepository lockRepository,
            TransactionTemplate tt,
            MembershipCardService cardService, StatisticDao statisticDao) {
        this.defaultArenaId = defaultArenaId;
        this.defaultCourtId = defaultCourtId;
        this.billDao = billDao;
        this.bookingMapper = bookingMapper;
        this.mcDao = mcDao;
        this.arenaDao = arenaDao;
        this.schedulerRepository = schedulerRepository;
        this.lockRepository = lockRepository;
        this.tt = tt;
        this.cardService = cardService;
        this.statisticDao = statisticDao;
    }

    @PostMapping("/booking")
    @Transactional
    public void booking(User user,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam Date date,
                        @Min(0) @Max(47) @RequestParam int timeIndexStart,
                        @Min(0) @Max(47) @RequestParam int timeIndexEnd) {
        //todo 正式上线后面删除
        MembershipCard card = cardService.find(user.getOpenId(), this.defaultArenaId);

        if (card.getBalance() <= 0) {
            throw new BusinessException("账户余额不足");
        }
        this.booking(user, date, this.defaultArenaId, new int[]{this.defaultCourtId}, new int[]{timeIndexStart}, new int[]{timeIndexEnd}, -1, card.getCode());
    }

    @PostMapping("/v2/booking")
    public Map<String, String> booking(User user,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                       @RequestParam int arenaId,
                                       @RequestParam int[] courtIds,
                                       @RequestParam int[] startTimes,
                                       @RequestParam int[] endTimes,
                                       @RequestParam int totalFee,
                                       @RequestParam(required = false) String code) {
        Arena dbArena = arenaDao.loadFull(arenaId);
        ArenaHelper.verifyStatus(dbArena);

        LockRepository.Lock lock = lockRepository.require(arenaId);
        try {
            return tt.execute(status -> doBook(user, dbArena, date, arenaId, courtIds, startTimes, endTimes, totalFee, code));
        } finally {
            lock.unlock();
        }
    }

    @GetMapping("/v2/bookings")
    public List<Booking> bookings(User user, @RequestParam(required = false) Boolean history) {
        Calendar now = Calendar.getInstance();
        return bookingMapper.userBookings(user.getOpenId(), DateUtils.truncate(now, Calendar.DAY_OF_MONTH).getTime(), history != null && history);
    }

    @GetMapping("/booking/order")
    public OrderDetail getBookingOrder(User user,
                                       @RequestParam @NotEmpty String payNo,
                                       @RequestParam @NotEmpty String payType) {
        OrderDetail detail = new OrderDetail();
        detail.setPayType(payType);
        detail.setPayNo(payNo);
        if ("mc".equals(payType)) {
            MembershipCardBill bill = billDao.load(payNo);
            detail.setFee(bill.getFee());
            detail.setCode(bill.getCode());
            detail.setCreateTime(bill.getCreateTime());
        } else {
            Trade trade = payDao.load(payNo);
            detail.setTransactionId(trade.getTransactionId());
            detail.setMcId(trade.getMchId());
            detail.setFee(trade.getFee());
            detail.setCreateTime(trade.getCreateTime());
        }
        detail.setBookings(bookingMapper.byPayNo(payNo));
        return detail;
    }

    @DeleteMapping("/booking/{id}")
    @Transactional
    public synchronized void cancelNotCharged(User user, @PathVariable int id) {
        Booking booking = bookingMapper.selectByPrimaryKey(id);
        Validate.notNull(booking);
        if (booking.getCharged()) {
            throw new BusinessException("已支付订场不能取消");
        }

        validateCancelAble(user.getOpenId(), Collections.singletonList(booking));

        bookingMapper.deleteBooking(booking);
        bookingMapper.deleteShare(booking.getId());
        context.publishEvent(new BookingCancelEvent(this, user, booking));
    }

    @DeleteMapping("/v2/refund")
    @Transactional
    public synchronized void refund(User user, @RequestParam @NotEmpty String payNo) {
        List<Booking> bookings = bookingMapper.byPayNo(payNo);
        Validate.notEmpty(bookings);
        Booking peek = bookings.get(0);
        if (!peek.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地不是你定的哦");
        }
        if (!peek.getStatus().equals("ok")) {
            throw new BusinessException("场地状态错误:" + peek.getStatus());
        }
        if (!peek.getCharged()) {
            throw new BusinessException("为支付的场地无法退款");
        }
        if ("wep".equals(peek.getPayType())) {
            throw new BusinessException("微信暂时支付不支持取消");
        }

        validateCancelAble(user.getOpenId(), bookings);

        cardService.refund(user, peek.getPayNo());
        bookingMapper.updateBookingStatus(peek.getPayNo(), "rfd");

        //todo 退分摊费用,目前已付费的是分摊不了的,所以暂不考虑
        for (Booking booking : bookings) {
            context.publishEvent(new BookingCancelEvent(this, user, booking));
        }
    }

    @Override
    @Transactional
    public void onApplicationEvent(TradeEvent te) {
        Trade trade = te.getTrade();
        if (!Constant.PRODUCT_BOOKING.equals(trade.getProductType())) {
            return;
        }
        log.info("receive booking trade[{}] event, in status {}", trade.getTradeNo(), trade.getStatus());
        if (!("ok".equals(trade.getStatus()) || "wp".equals(trade.getStatus()))) {
            List<Booking> bookings = bookingMapper.byPayNo(trade.getTradeNo());
            Booking first = bookings.get(0);
            if (!first.getStatus().equals("ok")) {
                log.warn("booking in trade {} already in status {}", trade.getTradeNo(), first.getStatus());
                return;
            }
            bookingMapper.updateBookingStatus(trade.getTradeNo(), "pf");
            for (Booking booking : bookings) {
                context.publishEvent(new BookingCancelEvent(this, null, booking));
            }
        }
    }

    private Map<String, String> doBook(User user, Arena dbArena, Date date, int arenaId, int[] courtIds, int[] startTimes, int[] endTimes, int totalFee, String code) {
        if (code == null && payDao.hasWaitForPay(user.getOpenId())) {
            throw new BusinessException("您有一个未支付的预定待系统确认,请10分钟后再试!");
        }

        //检查累计预定时间是否超过场馆设置
        validateHourLimit(user, dbArena, startTimes, endTimes);

        int tf = 0;
        boolean postCharge = code != null && dbArena.getChargeStrategy() != 0;
        String tradeNo = postCharge ? null : WePayService.creatTradeNo(Constant.PRODUCT_BOOKING);
        BookScheduler scheduler = schedulerRepository.arenaScheduler(dbArena);
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < courtIds.length; i++) {
            int courtId = courtIds[i];
            int startTime = startTimes[i];
            int endTime = endTimes[i];

            scheduler.book(date, courtId, startTime, endTime);

            ArenaHelper.verifyRules(dbArena, courtId, date, startTime);
            int fee = ArenaHelper.calcFee(dbArena, date, startTime, endTime, courtId);

            Booking booking = new Booking();
            Arena arena = new Arena();
            arena.setId(arenaId);
            booking.setArena(arena);
            Court court = new Court();
            booking.setCourt(court);
            court.setId(courtId);
            booking.setDate(date);
            booking.setStart(startTime);
            booking.setEnd(endTime);
            booking.setOpenId(user.getOpenId());
            booking.setFee(fee);
            booking.setPayType(code == null ? "wep" : "mc");
            if (postCharge) {
                //会员卡支付且不是立即收费的,保存会员卡号到payNo,定时任务从这个卡里扣钱
                booking.setPayNo(code);
                booking.setCharged(false);
            } else {
                booking.setPayNo(tradeNo);
                booking.setCharged(true);
            }
            tf = tf + fee;
            bookings.add(booking);
        }

        try {
            //todo 全面上线后这个-1就不要
            if (totalFee != -1 && tf != totalFee) {
                throw new BusinessException("对不起,总费用不匹配,请联系管理员处理!");
            }
            for (Booking booking : bookings) {
                bookingMapper.insert(booking);
                context.publishEvent(new BookingEvent(this, user, booking));
            }
            if (postCharge) {
                return null;
            } else if (code != null) {
                membershipCardPay(user, dbArena, tradeNo, code, tf);
                return null;
            } else {
                return preparePay(tradeNo, dbArena.getMchId(), user.getOpenId(), Constant.PRODUCT_BOOKING, tf, "场地预定", () -> {
                    //do nothing
                });
            }
        } catch (Throwable e) {
            scheduler.invalidate();
            throw e;
        }
    }

    private void validateCancelAble(String openId, List<Booking> bookings) {
        Arena arena = arenaDao.loadFull(bookings.get(0).getArena().getId());

        //20分钟内的可以直接推定,不记录推定次数
        for (Booking booking : bookings) {
            if (System.currentTimeMillis() - booking.getUpdateTime().getTime() < 20 * DateUtils.MILLIS_PER_MINUTE) {
                return;
            }
        }

        for (Booking booking : bookings) {
            Date start = BookingTool.toBookingDate(booking.getDate(), booking.getStart());
            if (start.getTime() - System.currentTimeMillis() < arena.getRefundAdvanceHours() * DateUtils.MILLIS_PER_HOUR) {
                throw new BusinessException("预定场地必须提前" + arena.getRefundAdvanceHours() + "小时取消,如有特殊需求请联系管理员");
            }
        }
        Date date = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        int times = statisticDao.getCancelTimes(openId, date);
        if (times >= arena.getRefundTimesLimit()) {
            throw new BusinessException("本月退定已超过限额");
        }
        statisticDao.setCancelTimes(openId, date, times + 1);
    }

    private void validateHourLimit(User user, Arena arena, int[] startTimes, int[] endTimes) {
        if (arena.getBookHoursLimit() <= 0) {
            return;
        }
        int count = 0;
        for (int i = 0; i < startTimes.length; i++) {
            int startTime = startTimes[i];
            int endTime = endTimes[i];
            count = count + endTime - startTime + 1;
        }
        Calendar now = Calendar.getInstance();
        int start = now.get(Calendar.HOUR_OF_DAY);
        now = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
        if (arena.getBookHoursLimit() * 2 < bookingMapper.countBooked(user.getOpenId(), arena.getId(), now.getTime(), start) + count) {
            throw new BusinessException("该场馆最多只允许预定" + arena.getBookHoursLimit() + "个小时");
        }
    }

    private void membershipCardPay(User user, Arena arena, String tradeNo, String code, int totalFee) {
        MembershipCard card = mcDao.loadCard(code);
        ArenaHelper.verifyHasMeta(arena, card.getMeta().getId());
        if (!card.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("不是您的卡");
        }
        if (card.getExpireDate().getTime() < System.currentTimeMillis()) {
            throw new BusinessException("卡已经过期");
        }
        cardService.charge(tradeNo, user.getOpenId(), totalFee, Constant.PRODUCT_BOOKING, card, false);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
