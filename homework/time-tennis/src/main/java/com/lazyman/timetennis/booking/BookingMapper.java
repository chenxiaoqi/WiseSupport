package com.lazyman.timetennis.booking;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface BookingMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_booking
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_booking
     *
     * @mbg.generated
     */
    int insert(Booking record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_booking
     *
     * @mbg.generated
     */
    Booking selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_booking
     *
     * @mbg.generated
     */
    List<Booking> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_booking
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Booking record);


    List<Booking> queryByDate(@Param("date") Date date);

    List<Booking> queryInRange(@Param("start") Date start, @Param("end") Date end);

    int countBooked(@Param("openId") String openId, @Param("date") Date date, @Param("start") int startIndex);

    List<Booking> query(@Param("openId") String openId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    int deleteBooking(Booking booking);

    void addShare(@Param("bookingId") int bookingId, @Param("openId") String openId);

    void deleteShare(@Param("bookingId") int bookingId);

    List<Booking> page(@Param("id") Integer id);
}