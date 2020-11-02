package com.lazyman.timetennis.booking;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface BookingMapper {

    int insert(Booking record);

    Booking selectByPrimaryKey(Integer id);

    List<Booking> queryInRange(@Param("arenaId") Integer arenaId, @Param("start") Date start, @Param("end") Date end);

    int countBooked(@Param("openId") String openId, @Param("arenaId") int arenaId, @Param("date") Date date, @Param("start") int startIndex);

    List<Booking> queryInDateRange(QueryParam param);

    void deleteBooking(Booking booking);

    void addShare(@Param("bookingId") int bookingId, @Param("openId") String openId, @Param("payNo") String payNo);

    void deleteShare(@Param("bookingId") int bookingId);

    List<Booking> userBookings(@Param("openId") String openId, @Param("now") Date date, @Param("history") boolean history);

    void updateBookingStatus(@Param("tradeNo") String tradeNo, @Param("status") String status);

    List<Booking> byPayNo(@Param("payNo") String payNo);

    void setCharged(@Param("id") int id, @Param("payNo") String payNo);

    void setSharePayNo(@Param("bookingId") Integer bookingId, @Param("openId") String openId, @Param("payNo") String payNo);
}