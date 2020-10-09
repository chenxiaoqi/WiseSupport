package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.Rule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class BookingTool {

    public static final FastDateFormat DESC_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd E", Locale.CHINA);

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

    static int calcFee(List<Rule> rules, Date date, int timeIndexStart, int timeIndexEnd, int defaultFee, int courtId) {
        String dateString = Constant.FORMAT.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int fee = 0;
        for (int i = timeIndexStart; i <= timeIndexEnd; i++) {
            Rule find = null;
            for (Rule rule : rules) {
                if (rule.getType() == 2) {
                    if (rule.getStartDate() == null || dateString.compareTo(rule.getStartDate()) >= 0 && dateString.compareTo(rule.getEndDate()) < 0) {
                        if (rule.getWeek() == null || week == rule.getWeek()) {
                            if (rule.getStartHour() == null || i >= rule.getStartHour() * 2 && i < rule.getEndHour() * 2) {
                                find = rule;
                                break;
                            }
                        }
                    }
                }
            }
            if (find != null) {
                fee = fee + find.getFee() / 2;
            } else {
                fee = fee + defaultFee / 2;
            }
        }
        return fee;
    }

    static int calcFeeV2(List<Rule> rules, Date date, int timeIndexStart, int defaultFee, int courtId) {
        String dateString = Constant.FORMAT.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        Rule find = null;
        for (Rule rule : rules) {
            if (rule.getType() == 2) {
                if (rule.getStartDate() == null || dateString.compareTo(rule.getStartDate()) >= 0 && dateString.compareTo(rule.getEndDate()) < 0) {
                    if (rule.getWeek() == null || week == rule.getWeek()) {
                        if (rule.getStartHour() == null || timeIndexStart >= rule.getStartHour() * 2 && timeIndexStart < rule.getEndHour() * 2) {
                            find = rule;
                            break;
                        }
                    }
                }
            }
        }
        return find != null ? find.getFee() : defaultFee;
    }

    public static void main(String[] args) throws ParseException {

    }

    static boolean isBookable(List<Rule> rules, Date date, int start, int end) {
        String dateString = Constant.FORMAT.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        for (Rule rule : rules) {
            if (rule.getType() == 1) {
                if (rule.getStartDate() == null || dateString.compareTo(rule.getStartDate()) >= 0 && dateString.compareTo(rule.getEndDate()) < 0) {

                    if (rule.getWeek() == null || week == rule.getWeek()) {
                        if (rule.getStartHour() == null || start >= rule.getStartHour() * 2 && end < rule.getEndHour() * 2) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static String toDescription(Booking booking) {
        return toDescription(booking.getDate(), booking.getStart(), booking.getEnd());
    }

    public static String toDescription(Date date, int start, int end) {
        return DESC_FORMAT.format(date) + ' ' +
                toTime(start) +
                '~' +
                toTime(end + 1);
    }

    public static String toTime(int start) {
        return StringUtils.leftPad(String.valueOf(start / 2), 2, '0') + ':' +
                ((start & 1) == 0 ? "00" : "30");
    }
}
