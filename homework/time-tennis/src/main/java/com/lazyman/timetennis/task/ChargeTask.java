package com.lazyman.timetennis.task;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.booking.QueryParam;
import com.lazyman.timetennis.booking.Share;
import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.menbership.MembershipCardService;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.WePayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
public class ChargeTask implements Runnable {
    private final int chargeStrategy;
    private final BookingMapper bookingMapper;
    private final ArenaDao arenaDao;
    private final MembershipCardService cardService;
    private final MembershipCardDao mcDao;
    private final TransactionTemplate tt;
    private String today;

    public ChargeTask(int chargeStrategy, BookingMapper bookingMapper, ArenaDao arenaDao, MembershipCardService cardService, MembershipCardDao mcDao, TransactionTemplate tt) {
        this.chargeStrategy = chargeStrategy;
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
        this.cardService = cardService;
        this.mcDao = mcDao;
        this.tt = tt;
        if (chargeStrategy != 1 & chargeStrategy != 2) {
            throw new IllegalStateException("invalid charge strategy");
        }
    }

    public void run() {
        Date start;
        Date end;
        if (chargeStrategy == 1) {
            end = DateUtils.truncate(getToday(), Calendar.DAY_OF_MONTH);
            start = DateUtils.addDays(end, -1);
        } else {
            end = DateUtils.truncate(getToday(), Calendar.MONTH);
            start = DateUtils.addMonths(end, -1);
        }

        List<Integer> arenaIds = arenaDao.arenaIdsByChargeStrategy(this.chargeStrategy);
        if (arenaIds.isEmpty()) {
            log.info("no arena found with charge strategy {}", this.chargeStrategy);
        } else {
            long startTime = System.currentTimeMillis();
            for (Integer arenaId : arenaIds) {
                log.info("start charge arena {} {} ~ {},charge strategy {}", arenaId, Constant.FORMAT.format(start), Constant.FORMAT.format(end), this.chargeStrategy);
                charge(arenaId, start, end);
                log.info("end charge arena {},elapse {}s", arenaId, (System.currentTimeMillis() - startTime) / 1000);
            }
        }
    }

    private void charge(Integer arenaId, Date start, Date end) {
        QueryParam param = new QueryParam();
        param.setStart(start);
        param.setEnd(end);
        param.setArenaId(arenaId);
        param.setStatus("ok");
        param.setCharged(false);
        List<Booking> bookings = bookingMapper.queryInDateRange(param);
        for (Booking booking : bookings) {
            try {
                tt.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                        MembershipCard card = mcDao.loadCard(booking.getPayNo());
                        int fee = booking.getFee();
                        boolean hasShared = !CollectionUtils.isEmpty(booking.getShareUsers());
                        if (hasShared) {
                            int average = booking.getFee() / (booking.getShareUsers().size() + 1);
                            for (Share share : booking.getShareUsers()) {
                                String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_BOOKING_SHARE);
                                cardService.charge(
                                        tradeNo,
                                        User.SYSTEM_USER,
                                        average,
                                        Constant.PRODUCT_BOOKING_SHARE,
                                        mcDao.loadCard(share.getPayNo()), true, booking.getUpdateTime());
                                bookingMapper.setSharePayNo(booking.getId(), share.getOpenId(), tradeNo);
                            }
                            fee = fee - average * booking.getShareUsers().size();
                        }
                        String productType = hasShared ? Constant.PRODUCT_BOOKING_SHARE : Constant.PRODUCT_BOOKING;
                        String tradeNo = WePayService.creatTradeNo(productType);
                        cardService.charge(tradeNo, User.SYSTEM_USER, fee, productType, card, true, booking.getUpdateTime());
                        bookingMapper.setCharged(booking.getId(), tradeNo);
                    }
                });
            } catch (Exception e) {
                log.error("charge booking {} failed", booking.getId(), e);
            }
        }
    }

    private Date getToday() {
        try {
            return today == null ? new Date() : Constant.FORMAT.parse(today);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setToday(String today) {
        this.today = today;
    }
}
