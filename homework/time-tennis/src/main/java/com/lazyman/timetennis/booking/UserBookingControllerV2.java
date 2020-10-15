package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.core.LockRepository;
import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardBillDao;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.BasePayController;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user/v2")
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

    public UserBookingControllerV2(MembershipCardBillDao billDao,
                                   BookingMapper bookingMapper,
                                   MembershipCardDao mcDao,
                                   ArenaDao arenaDao,
                                   BookSchedulerRepository schedulerRepository,
                                   LockRepository lockRepository,
                                   TransactionTemplate tt) {
        this.billDao = billDao;
        this.bookingMapper = bookingMapper;
        this.mcDao = mcDao;
        this.arenaDao = arenaDao;
        this.schedulerRepository = schedulerRepository;
        this.lockRepository = lockRepository;

        this.tt = tt;
    }

    @PostMapping("/booking")
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

    @GetMapping("/bookings")
    public List<Booking> bookings(User user, @RequestParam(required = false) Boolean history) {
        Calendar now = Calendar.getInstance();
        return bookingMapper.userBookings(user.getOpenId(), DateUtils.truncate(now, Calendar.DAY_OF_MONTH).getTime(), history != null && history);
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

        int tf = 0;
        String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_BOOKING);
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
            booking.setPayNo(tradeNo);
            booking.setCharged(true);
            booking.setPayType(code == null ? "wep" : "mc");
            tf = tf + fee;
            bookings.add(booking);
        }

        try {
            if (tf != totalFee) {
                throw new BusinessException("对不起,总费用不匹配,请联系管理员处理!");
            }
            for (Booking booking : bookings) {
                bookingMapper.insert(booking);
            }

            if (code != null) {
                membershipCardPay(user, dbArena, tradeNo, code, totalFee);
                return null;
            } else {
                return preparePay(tradeNo, dbArena.getMchId(), user.getOpenId(), Constant.PRODUCT_BOOKING, totalFee, "场地预定", () -> {
                    //do nothing
                });
            }
        } catch (Throwable e) {
            scheduler.invalidate();
            throw e;
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
        totalFee = totalFee * card.getMeta().getDiscount() / 100;
        int balance = card.getBalance();
        if (totalFee != 0) {
            balance = mcDao.chargeFee(code, totalFee);
        }
        billDao.add(tradeNo, user.getOpenId(), code, Constant.PRODUCT_BOOKING, totalFee, balance);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
