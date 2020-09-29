package com.lazyman.timetennis.booking;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface BookingMapper {

    int insert(Booking record);

    Booking selectByPrimaryKey(Integer id);

    List<Booking> queryByDate(@Param("date") Date date, @Param("arenaId") int arenaId);

    List<Booking> queryInRange(@Param("arenaId") Integer arenaId, @Param("start") Date start, @Param("end") Date end);

    int countBooked(@Param("openId") String openId, @Param("date") Date date, @Param("start") int startIndex);

    List<Booking> query(@Param("openId") String openId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    void deleteBooking(Booking booking);

    void addShare(@Param("bookingId") int bookingId, @Param("openId") String openId);

    void deleteShare(@Param("bookingId") int bookingId);

    List<Booking> page(@Param("openId") String openId, @Param("id") Integer id);

    List<Booking> userBookings(@Param("openId") String openId, @Param("now") Date date, @Param("history") boolean history);

    void updateBookingStatus(@Param("tradeNo") String tradeNo, @Param("status") String status);
}