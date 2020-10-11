package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.*;
import com.lazyman.timetennis.core.LockRepository;
import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardBillDao;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.BasePayController;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/v2")
@Slf4j
public class UserBookingControllerV2 extends BasePayController implements ApplicationListener<TradeEvent> {
    private MembershipCardBillDao billDao;

    private RuleDao ruleDao;

    private CourtDao courtDao;

    private BookingMapper bookingMapper;

    private MembershipCardDao mcDao;

    private ArenaDao arenaDao;

    private LockRepository lockRepository = new LockRepository();

    public UserBookingControllerV2(MembershipCardBillDao billDao, RuleDao ruleDao,
                                   CourtDao courtDao,
                                   BookingMapper bookingMapper,
                                   MembershipCardDao mcDao, ArenaDao arenaDao) {
        this.billDao = billDao;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.bookingMapper = bookingMapper;
        this.mcDao = mcDao;
        this.arenaDao = arenaDao;
    }

    @PostMapping("/booking")
    @Transactional
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
            if (code == null && payDao.hasWaitForPay(user.getOpenId())) {
                throw new BusinessException("您有一个未支付的预定待系统确认,请10分钟后再试!");
            }

            List<Booking> bookings = bookingMapper.queryByDate(date, arenaId);
            int tf = 0;
            String tradeNo = pay.creatTradeNo(Constant.PRODUCT_BOOKING);
            for (int i = 0; i < courtIds.length; i++) {
                int courtId = courtIds[i];
                int startTime = startTimes[i];
                int endTime = endTimes[i];

                ArenaHelper.verifyRules(dbArena, courtId, date, startTime);

                for (Booking booking : bookings) {
                    if (courtId == booking.getCourt().getId()) {
                        if (!(startTime < booking.getStart() && endTime < booking.getStart() || startTime > booking.getEnd() && endTime > booking.getEnd())) {
                            throw new BusinessException("对不起,该时间段已被预定");
                        }
                    }
                }
                int fee = ArenaHelper.calcFee(dbArena, date, startTime, endTime, courtId);

                Booking booking = new Booking();
                Arena arena = new Arena();
                arena.setId(arenaId);
                booking.setArena(arena);
                Court court = new Court();
                court.setId(courtId);
                booking.setCourt(court);
                booking.setDate(date);
                booking.setStart(startTime);
                booking.setEnd(endTime);
                booking.setOpenId(user.getOpenId());
                booking.setFee(fee);
                booking.setPayNo(tradeNo);
                booking.setPayType(code == null ? "wep" : "mc");
                bookingMapper.insert(booking);
                tf = tf + fee;
            }
            if (tf != totalFee) {
                throw new BusinessException("对不起,总费用不匹配,请联系管理员处理!");
            }

            if (code != null) {
                membershipCardPay(user, dbArena, tradeNo, code, totalFee);
                return null;
            } else {
                return preparePay(tradeNo, dbArena.getMchId(), user.getOpenId(), Constant.PRODUCT_BOOKING, totalFee, "场地预定", () -> {
                    //do nothing
                });
            }
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
            bookingMapper.updateBookingStatus(trade.getTradeNo(), "pf");
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
        if (totalFee != 0) {
            Validate.isTrue(mcDao.chargeFee(code, totalFee) == 1, "余额不足");
        }
        billDao.add(tradeNo, user.getOpenId(), code, Constant.PRODUCT_BOOKING, totalFee, card.getBalance() - totalFee);
    }
}
