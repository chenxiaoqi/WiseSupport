package com.lazyman.timetennis.task;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.menbership.MembershipCard;
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
    private int chargeStrategy;
    private BookingMapper bookingMapper;
    private ArenaDao arenaDao;
    private MembershipCardService cardService;
    private TransactionTemplate tt;

    private Date start;
    private Date end;
    private String today;


    public ChargeTask(int chargeStrategy, BookingMapper bookingMapper, ArenaDao arenaDao, MembershipCardService cardService, TransactionTemplate tt) {
        this.chargeStrategy = chargeStrategy;
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
        this.cardService = cardService;
        this.tt = tt;
        if (chargeStrategy != 1 & chargeStrategy != 2) {
            throw new IllegalStateException("invalid charge strategy");
        }
    }

    public void run() {
        if (chargeStrategy == 1) {
            this.end = DateUtils.truncate(getToday(), Calendar.DAY_OF_MONTH);
            this.start = DateUtils.addDays(end, -1);
        } else {
            this.end = DateUtils.truncate(getToday(), Calendar.MONTH);
            this.start = DateUtils.addMonths(end, -1);
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
        List<Booking> bookings = bookingMapper.notCharged(arenaId, start, end);
        for (Booking booking : bookings) {
            tt.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                    try {
                        MembershipCard card = cardService.find(booking.getOpenId(), booking.getArena().getId());
                        int fee = booking.getFee();
                        boolean hasShared = !CollectionUtils.isEmpty(booking.getShareUsers());
                        if (hasShared) {
                            int average = booking.getFee() / (booking.getShareUsers().size() + 1);
                            for (User shareUser : booking.getShareUsers()) {
                                String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_BOOKING_SHARE);
                                cardService.charge(
                                        tradeNo,
                                        User.SYSTEM_USER,
                                        average,
                                        Constant.PRODUCT_BOOKING_SHARE,
                                        cardService.find(shareUser.getOpenId(), booking.getArena().getId()), true);
                                bookingMapper.setSharePayNo(booking.getId(), shareUser.getOpenId(), tradeNo);
                            }
                            fee = fee - average * booking.getShareUsers().size();
                        }
                        String productType = hasShared ? Constant.PRODUCT_BOOKING_SHARE : Constant.PRODUCT_BOOKING;
                        String tradeNo = WePayService.creatTradeNo(productType);
                        cardService.charge(tradeNo, User.SYSTEM_USER, fee, productType, card, true);
                        bookingMapper.setCharged(booking.getId(), tradeNo);
                    } catch (Exception e) {
                        log.error("charge booking {} failed", booking.getId());
                    }
                }
            });
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
