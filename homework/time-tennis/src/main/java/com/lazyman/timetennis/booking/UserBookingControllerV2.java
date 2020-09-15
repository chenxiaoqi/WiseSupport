package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Date;

@RestController
@RequestMapping("/user/v2")
public class UserBookingControllerV2 {

    private ArenaDao arenaDao;


    public UserBookingControllerV2(ArenaDao arenaDao) {
        this.arenaDao = arenaDao;
    }

    @PostMapping("/booking")
    @Transactional
    public synchronized void booking(@SessionAttribute("user") User user,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                     int arenaId,
                                     int[] courtIds,
                                     int[] startTimes,
                                     float totalFee) {


    }

}
