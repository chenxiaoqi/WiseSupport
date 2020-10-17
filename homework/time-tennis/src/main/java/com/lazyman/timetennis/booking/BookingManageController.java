package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaPrivilege;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.menbership.MembershipCardService;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.Trade;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class BookingManageController implements ApplicationContextAware {
    private BookingMapper bookingMapper;

    private PayDao payDao;

    private ArenaPrivilege privilege;

    private BookSchedulerRepository repository;

    private ApplicationContext context;

    private MembershipCardService cardService;

    public BookingManageController(BookingMapper bookingMapper, PayDao payDao, ArenaPrivilege privilege, BookSchedulerRepository repository, MembershipCardService cardService) {
        this.bookingMapper = bookingMapper;
        this.payDao = payDao;
        this.cardService = cardService;
        this.privilege = privilege;
        this.repository = repository;
    }

    @PostMapping("/court/lock")
    @Transactional
    public synchronized void lock(User user,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                  @RequestParam int arenaId,
                                  @RequestParam int[] courtIds,
                                  @RequestParam int[] startTimes,
                                  @RequestParam int[] endTimes) {

        privilege.requireAdministrator(user.getOpenId(), arenaId);

        BookScheduler scheduler = repository.arenaScheduler(arenaId);
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < courtIds.length; i++) {
            int courtId = courtIds[i];
            int startTime = startTimes[i];
            int endTime = endTimes[i];

            scheduler.book(date, courtId, startTime, endTime);

            Booking booking = new Booking();
            Arena arena = new Arena();
            arena.setId(arenaId);
            booking.setArena(arena);
            Court court = new Court();
            court.setId(courtId);
            booking.setDate(date);
            booking.setCourt(court);
            booking.setStart(startTime);
            booking.setEnd(endTime);
            booking.setCharged(true);
            booking.setOpenId(user.getOpenId());
            booking.setFee(0);
            bookings.add(booking);
        }

        try {
            for (Booking booking : bookings) {
                bookingMapper.insert(booking);
            }
        } catch (Throwable e) {
            scheduler.invalidate();
            throw e;
        }
    }

    @PostMapping("/court/release")
    public void releaseCourt(User user,
                             @RequestParam int bookingId) {
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        if (booking.getFee() >= 0 && booking.getCharged()) {
            throw new BusinessException("已经收费,场地费用大于0");
        }

        privilege.requireAdministrator(user.getOpenId(), booking.getArena().getId());

        BookScheduler scheduler = repository.arenaScheduler(booking.getArena().getId());
        context.publishEvent(new BookingCancelEvent(this, user, booking));

        try {
            booking = new Booking();
            booking.setId(bookingId);
            bookingMapper.deleteBooking(booking);
        } catch (Throwable e) {
            scheduler.invalidate();
            throw e;
        }
    }

    @PostMapping("/booking/refund")
    @Transactional
    public synchronized void refund(User user,
                                    @RequestParam @NotEmpty String payType,
                                    @RequestParam @NotEmpty String payNo) {
        List<Booking> bookings = bookingMapper.byPayNo(payNo);
        Validate.notEmpty(bookings);

        Booking peek = bookings.get(0);
        privilege.requireAdministrator(user.getOpenId(), peek.getArena().getId());

        for (Booking booking : bookings) {
            Validate.isTrue(booking.getStatus().equals("ok"), "预定记录状态错误[%s]", booking.getStatus());
        }


        if ("mc".equals(payType)) {
            //会员卡支付
            if (peek.getCharged()) {
                //已经付钱了,要退钱
                cardService.refund(user, payNo);
            }
        } else {
            Trade trade = payDao.load(payNo);
            Validate.notNull(trade);
            if (!trade.getStatus().equals("ok")) {
                throw new BusinessException("未支付成功订单,无法退订,当前状态: " + trade.getStatus());
            }
            payDao.updateStatus(payNo, "rfd");
        }
        bookingMapper.updateBookingStatus(payNo, "rfd");

        for (Booking booking : bookings) {
            context.publishEvent(new BookingCancelEvent(this, user, booking));
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
