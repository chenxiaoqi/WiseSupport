package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserBookingController implements ApplicationContextAware {
    private BookingMapper bookingMapper;

    private MembershipCardDao mcDao;

    private ApplicationContext application;

    private int defaultArenaId;

    public UserBookingController(BookingMapper bookingMapper,
                                 MembershipCardDao mcDao,
                                 @Value("${wx.default-arena-id}") int defaultArenaId) {
        this.bookingMapper = bookingMapper;
        this.mcDao = mcDao;
        this.defaultArenaId = defaultArenaId;
    }

    @PostMapping("/share/booking/{bookingId}")
    public void shareBooking(User user, @PathVariable int bookingId) {
        Booking booking = bookingMapper.selectByPrimaryKey(bookingId);
        Validate.notNull(booking);

        List<MembershipCard> cards = mcDao.userCardsInArena(user.getOpenId(), this.defaultArenaId);
        if (cards.isEmpty()) {
            throw new BusinessException("对不起,您不是这个场馆的会员,不能预定场地");
        }

        if (booking.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("这个场地就是你定的哦");
        }

        if (booking.getCharged()) {
            throw new BusinessException("已出账单,不能分摊了哦");
        }

        try {
            bookingMapper.addShare(booking.getId(), user.getOpenId(), cards.get(0).getCode());
        } catch (DuplicateKeyException e) {
            throw new BusinessException("您已经分摊过了");
        }
        application.publishEvent(new BookingShareEvent(this, user, booking));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }
}
