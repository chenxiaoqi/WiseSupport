package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.*;
import com.lazyman.timetennis.core.SecurityUtils;
import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserBookingControllerV2 {
    private String appId;

    private String platformMchId;

    private RuleDao ruleDao;

    private CourtDao courtDao;

    private BookingMapper bookingMapper;

    private WePayService pay;

    private PayDao payDao;

    private MembershipCardDao mcDao;

    public UserBookingControllerV2(@Value("${wx.app-id}") String appId,
                                   @Value("${wx.mch-id}") String platformMchId,
                                   RuleDao ruleDao,
                                   CourtDao courtDao,
                                   BookingMapper bookingMapper,
                                   WePayService pay,
                                   PayDao payDao,
                                   MembershipCardDao mcDao) {
        this.appId = appId;
        this.platformMchId = platformMchId;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.bookingMapper = bookingMapper;
        this.pay = pay;
        this.payDao = payDao;
        this.mcDao = mcDao;
    }

    @PostMapping("/user/v2/booking")
    @Transactional
    public synchronized Map<String, String> booking(@SessionAttribute("user") User user,
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
            return wePay(user, arenaId, totalFee, nbs);
        }
    }

    @GetMapping("/mine/bookings")
    public List<Booking> bookings(@SessionAttribute User user, Boolean history) {
        Calendar now = Calendar.getInstance();
        return bookingMapper.userBookings(user.getOpenId(), DateUtils.truncate(now, Calendar.DAY_OF_MONTH).getTime(), history != null && history);
    }

    private Map<String, String> wePay(User user, int arenaId, int totalFee, List<Booking> nbs) {
        String productType = Constant.PRODUCT_BOOKING;
        String tradeNo = pay.creatTradeNo(Constant.PRODUCT_BOOKING);

        //todo 商户ID要用场地对应商户ID，而不是平台的商户ID
        String prepayId = pay.prepay(this.platformMchId, user.getOpenId(), tradeNo, String.valueOf(totalFee), "场地预定");
        payDao.createTrade(tradeNo, user.getOpenId(), productType, prepayId, totalFee, arenaId, nbs, this.platformMchId);

        TreeMap<String, String> params = new TreeMap<>();
        params.put("appId", appId);
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", SecurityUtils.randomSeq(32));
        params.put("package", "prepay_id=" + prepayId);
        params.put("signType", "MD5");

        params.put("paySign", pay.createSign(params));

        //返回tradeNo让前台拿可以查询
        params.put("tradeNo", tradeNo);
        return params;
    }

    private void membershipCardPay(User user, String code, int totalFee) {
        MembershipCard card = mcDao.loadCard(code);
        if (!card.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("不是您的卡");
        }
        totalFee = totalFee * card.getMeta().getDiscount() / 100;
        if (totalFee != 0) {
            Validate.isTrue(mcDao.chargeFee(code, totalFee) == 1, "余额不足");
        }
        //todo 记录消费日志
    }

}
