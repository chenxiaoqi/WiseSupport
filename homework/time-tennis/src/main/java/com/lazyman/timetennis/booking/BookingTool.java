package com.lazyman.timetennis.booking;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class BookingTool {

    private static final FastDateFormat DESC_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd E", Locale.CHINA);

    private BookingTool() {
    }

    public static Date toBookingDate(Date date, int timeIndex) {
        return DateUtils.addMilliseconds(date, (int) (timeIndex * 30 * DateUtils.MILLIS_PER_MINUTE));
    }

    public static boolean cancelAble(Booking booking) {

        //已经出过账单了不能删除
        if (booking.getCharged()) {
            return false;
        }

        //刚刚定的都可以删除
        if (System.currentTimeMillis() - booking.getUpdateTime().getTime() < 20 * DateUtils.MILLIS_PER_MINUTE) {
            return true;
        }

        //必须提前2个小时取消场地预定
        Date start = toBookingDate(booking.getDate(), booking.getStart());
        if (start.getTime() - System.currentTimeMillis() < 2 * DateUtils.MILLIS_PER_HOUR) {
            return false;
        }
        return true;
    }

    static int calcFee(Date date, int timeIndexStart, int timeIndexEnd) {
        int fee = 0;
        for (int i = timeIndexStart; i <= timeIndexEnd; i++) {
            //晚上
            if (i >= 37) {
                fee = fee + 20;
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                //周末白天30
                if (weekDay == 1 || weekDay == 7) {
                    fee = fee + 15;
                }
                //工作日10
                else {
                    fee = fee + 10;
                }
            }
        }
        return fee;
    }

    public static void main(String[] args) throws ParseException {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        System.out.println(BookingTool.calcFee(format.parse("2020-06-6"), 36, 40));
    }

    static boolean isMemberTime(Date date, int start, int end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == Calendar.SUNDAY) {
            return start <= 37;
        } else {
            if (weekDay == Calendar.WEDNESDAY) {
                return end >= 38;
            }
        }
        return false;
    }

    public static String toDescription(Booking booking) {
        String builder = DESC_FORMAT.format(booking.getDate()) + ' ' +
                StringUtils.leftPad(String.valueOf(booking.getStart() / 2), 2, '0') + ':' +
                ((booking.getStart() & 1) == 0 ? "00" : "30") +
                '~' +
                StringUtils.leftPad(String.valueOf(booking.getEnd() / 2), 2, '0') + ':' +
                ((booking.getEnd() & 1) == 0 ? "00" : "30");
        return builder;
    }
}
