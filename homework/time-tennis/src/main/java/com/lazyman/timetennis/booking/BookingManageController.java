package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardBill;
import com.lazyman.timetennis.menbership.MembershipCardBillDao;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class BookingManageController {
    private BookingMapper bookingMapper;

    private ArenaDao arenaDao;

    private PayDao payDao;

    private MembershipCardBillDao billDao;

    private MembershipCardDao mcDao;

    private WePayService pay;

    public BookingManageController(BookingMapper bookingMapper, ArenaDao arenaDao, PayDao payDao, MembershipCardBillDao billDao, MembershipCardDao mcDao, WePayService pay) {
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
        this.payDao = payDao;
        this.billDao = billDao;
        this.mcDao = mcDao;
        this.pay = pay;
    }

    @PostMapping("/court/lock")
    @Transactional
    public synchronized void lock(User user,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                  @RequestParam int arenaId,
                                  @RequestParam int[] courtIds,
                                  @RequestParam int[] startTimes,
                                  @RequestParam int[] endTimes) {
        checkArenaPrivileges(user, arenaId);

        List<Booking> bookings = bookingMapper.queryByDate(date, arenaId);

        for (int i = 0; i < courtIds.length; i++) {
            int courtId = courtIds[i];
            int startTime = startTimes[i];
            int endTime = endTimes[i];


            for (Booking booking : bookings) {
                if (courtId == booking.getCourt().getId()) {
                    if (!(startTime < booking.getStart() && endTime < booking.getStart() || startTime > booking.getEnd() && endTime > booking.getEnd())) {
                        throw new BusinessException("对不起,该时间段已被预定");
                    }
                }
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
            booking.setFee(-1);
            bookingMapper.insert(booking);
        }
    }

    @PostMapping("/court/release")
    public void releaseCourt(User user,
                             @RequestParam int bookingId) {
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        checkArenaPrivileges(user, booking.getArena().getId());

        booking = new Booking();
        booking.setId(bookingId);
        booking.setOpenId(user.getOpenId());
        bookingMapper.deleteBooking(booking);
    }

    @GetMapping("/booking/order")
    public OrderDetail getBookingOrder(User user, int bookingId) {
        OrderDetail detail = new OrderDetail();
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        if (booking.getPayType() == null) {
            detail.setBookings(Collections.singletonList(booking));
            detail.setFee(booking.getFee());
        } else {
            detail.setPayType(booking.getPayType());
            detail.setPayNo(booking.getPayNo());
            if ("mc".equals(booking.getPayType())) {
                MembershipCardBill bill = billDao.load(booking.getPayNo());
                detail.setFee(bill.getFee());
                detail.setCode(bill.getCode());
                detail.setCreateTime(bill.getCreateTime());
            } else {
                Trade trade = payDao.load(booking.getPayNo());
                detail.setTransactionId(trade.getTransactionId());
                detail.setMcId(trade.getMchId());
                detail.setFee(trade.getFee());
                detail.setCreateTime(trade.getCreateTime());
            }
            detail.setBookings(bookingMapper.byPayNo(booking.getPayNo()));
        }
        return detail;
    }

    @PostMapping("/booking/refund")
    @Transactional
    public synchronized void refund(User user,
                                    @RequestParam @NotEmpty String payType,
                                    @RequestParam @NotEmpty String payNo) {
        List<Booking> bookings = bookingMapper.byPayNo(payNo);
        Validate.notEmpty(bookings);
        checkArenaPrivileges(user, bookings.get(0).getArena().getId());

        for (Booking booking : bookings) {
            Validate.isTrue(booking.getStatus().equals("ok"), "预定记录状态错误[%s]", booking.getStatus());
        }

        if ("mc".equals(payType)) {
            MembershipCardBill bill = billDao.load(payNo);
            Validate.notNull(bill);
            mcDao.recharge(bill.getCode(), bill.getFee());
            MembershipCard mc = mcDao.loadCard(bill.getCode());
            String tradeNo = pay.creatTradeNo(Constant.PRODUCT_REFUND);
            billDao.add(tradeNo, bill.getUser().getOpenId(), bill.getCode(), Constant.PRODUCT_REFUND, bill.getFee(), mc.getBalance());
        } else {
            Trade trade = payDao.load(payNo);
            Validate.notNull(trade);
            Validate.isTrue(trade.getStatus().equals("ok"), "未支付成功订单,无法退订,当前状态[%s]", trade.getStatus());
            payDao.updateStatus(payNo, "rfd");
        }

        bookingMapper.updateBookingStatus(payNo, "rfd");
    }

    private void checkPrivileges(User user) {
        if (!user.isArenaAdmin()) {
            throw new BusinessException("对不起,您没有权限没有权限");
        }
    }

    private void checkArenaPrivileges(User user, int arenaId) {
        checkPrivileges(user);
        if (!arenaDao.isArenaAdmin(user.getOpenId(), arenaId)) {
            throw new BusinessException("对不起,您不是该场馆管理员");
        }
    }
}
