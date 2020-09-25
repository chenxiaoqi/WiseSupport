package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.*;
import com.lazyman.timetennis.menbership.MembershipCard;
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

import java.util.*;

@RestController
@RequestMapping("/user/v2")
@Slf4j
public class UserBookingControllerV2 extends BasePayController implements ApplicationListener<TradeEvent> {

    private RuleDao ruleDao;

    private CourtDao courtDao;

    private BookingMapper bookingMapper;

    private MembershipCardDao mcDao;

    public UserBookingControllerV2(RuleDao ruleDao,
                                   CourtDao courtDao,
                                   BookingMapper bookingMapper,
                                   MembershipCardDao mcDao) {
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.bookingMapper = bookingMapper;
        this.mcDao = mcDao;
    }

    @PostMapping("/booking")
    @Transactional
    public synchronized Map<String, String> booking(User user,
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                    int arenaId,
                                                    int[] courtIds,
                                                    int[] startTimes,
                                                    int[] endTimes,
                                                    int totalFee,
                                                    @RequestParam(defaultValue = "1") int style,
                                                    String code) {
        if (code == null && payDao.hasWaitForPay(user.getOpenId())) {
            throw new BusinessException("您有一个未支付的预定待系统确认,请稍后再试!");
        }

        List<Booking> bookings = bookingMapper.queryByDate(date, arenaId);

        int tf = 0;
        List<Booking> nbs = new ArrayList<>();
        for (int i = 0; i < courtIds.length; i++) {
            int courtId = courtIds[i];
            int startTime = startTimes[i];
            int endTime = endTimes[i];

            List<Rule> rules = ruleDao.courtRules(new Object[]{courtId});
            if (!BookingTool.isBookable(rules, date, startTime, startTime + 1)) {
                throw new BusinessException("时间段不可预定");
            }
            for (Booking booking : bookings) {
                if (courtId == booking.getCourt().getId()) {
                    if (!(startTime < booking.getStart() && endTime < booking.getStart() || startTime > booking.getEnd() && endTime > booking.getEnd())) {
                        throw new BusinessException("对不起,该时间段已被预定");
                    }
                }
            }
            int fee;
            if (style == 2) {
                fee = BookingTool.calcFeeV2(rules, date, startTime, courtDao, courtId);
            } else {
                fee = BookingTool.calcFee(rules, date, startTime, endTime, courtDao, courtId);
            }
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
            bookingMapper.insert(booking);
            nbs.add(booking);
            tf = tf + fee;
        }
        if (tf != totalFee) {
            throw new BusinessException("对不起,总费用不匹配,请联系管理员处理!");
        }

        if (code != null) {
            membershipCardPay(user, code, totalFee);
            return null;
        } else {
            return wePay(user, totalFee, nbs);
        }
    }

    @GetMapping("/bookings")
    public List<Booking> bookings(User user, Boolean history) {
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
            payDao.deleteTradeBooking(trade.getTradeNo());
        }
    }

    private Map<String, String> wePay(User user, int totalFee, List<Booking> nbs) {
        String productType = Constant.PRODUCT_BOOKING;
        return preparePay(user.getOpenId(), productType, totalFee, "场地预定", tradeNo -> payDao.createTradeBookingRelation(tradeNo, nbs));

    }

    private void membershipCardPay(User user, String code, int totalFee) {
        MembershipCard card = mcDao.loadCard(code);
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
        //todo 记录消费日志
    }
}
