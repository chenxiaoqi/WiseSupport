package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/manage")
public class BookingManageController {
    private BookingMapper bookingMapper;

    private ArenaDao arenaDao;

    public BookingManageController(BookingMapper bookingMapper, ArenaDao arenaDao) {
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
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
