package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.booking.BookingTool;
import com.lazyman.timetennis.user.ChargeService;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MonthlyStatisticTask implements ApplicationContextAware {
    private BookingMapper bookingMapper;

    private UserMapper userMapper;

    private JdbcTemplate template;
    private ApplicationContext context;

    public MonthlyStatisticTask(BookingMapper bookingMapper, UserMapper userMapper, JdbcTemplate template, ChargeService service) {
        this.bookingMapper = bookingMapper;
        this.userMapper = userMapper;
        this.template = template;
    }

    //    @Scheduled(fixedDelay = 3600000)
    @Scheduled(cron = "${wx.stat-cron}")
    @Transactional
    public void run() {
        Date end = DateUtils.truncate(new Date(), Calendar.MONTH);
//        Date end = DateUtils.truncate(DateUtils.addMonths(new Date(), 1), Calendar.MONTH);
        Date start = DateUtils.addMonths(end, -1);

        log.info("monthly statistic task start {}", Constant.FORMAT.format(start));
        List<Booking> bookings = bookingMapper.query(null, start, end);
        Collection<Statistic> statistics = calc(bookings, start);

        User operator = userMapper.selectByPrimaryKey(User.SYSTEM_USER);
        Validate.notNull(operator);

        for (Statistic statistic : statistics) {
            userMapper.charge(statistic.getUser().getOpenId(), -statistic.getFee());
            User u = userMapper.selectByPrimaryKey(statistic.getUser().getOpenId());
            statistic.setBalance(u.getBalance());

            template.update("insert into monthly_stat (month ,open_id,fee,hours,book_times,balance)values(?,?,?,?,?,?)",
                    statistic.getMonth(),
                    statistic.getUser().getOpenId(),
                    statistic.getFee(),
                    statistic.getHours(),
                    statistic.getBookTimes(),
                    statistic.getBalance());
            for (BookingBill bill : statistic.getBills()) {
                template.update("insert into booking_bill (open_id, booking_id, fee,share, date, start, end) values (?,?,?,?,?,?,?)",
                        statistic.getUser().getOpenId(),
                        bill.getBookingId(),
                        bill.getFee(),
                        bill.isShare(),
                        bill.getDate(),
                        bill.getStart(),
                        bill.getEnd());
            }
        }
        for (Booking booking : bookings) {
            template.update("update tt_booking set charged= 1 where id=?", booking.getId());
        }

        try {
            writeExcel(start, putNoBooking(statistics));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        context.publishEvent(new MonthFeeEvent(this, operator));
    }

    private List<Statistic> putNoBooking(Collection<Statistic> statistics) {
        List<Statistic> result = new ArrayList<>(statistics);

        List<String> openIds = result.stream().map(stat -> stat.getUser().getOpenId()).collect(Collectors.toList());

        List<User> users = userMapper.queryExcluded(openIds);
        for (User user : users) {
            Statistic statistic = new Statistic();
            statistic.setUser(user);
            statistic.setBalance(user.getBalance());
            result.add(statistic);
        }
        return result;
    }

    private void writeExcel(Date start, Collection<Statistic> stats) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("总览");
        sheet.setColumnWidth(0, 256 * 20);
        HSSFRow head = sheet.createRow(0);
        head.createCell(0).setCellValue("网名");
        head.createCell(1, CellType.STRING).setCellValue("消费");
        head.createCell(2, CellType.STRING).setCellValue("余额");
        int index = 1;
        for (Statistic stat : stats) {
            HSSFRow row = sheet.createRow(index);
            String nickname = stat.getUser().getNickname() == null ? stat.getUser().getWxNickname() : stat.getUser().getWxNickname();
            row.createCell(0, CellType.STRING).setCellValue(nickname);
            row.createCell(1, CellType.STRING).setCellValue(stat.getFee());
            row.createCell(2, CellType.STRING).setCellValue(stat.getBalance());

            if (!stat.getBills().isEmpty()) {
                HSSFSheet billSheet = wb.createSheet(nickname);
                billSheet.setColumnWidth(0, 256 * 16);
                HSSFRow billHeadRow = billSheet.createRow(0);
                billHeadRow.createCell(0, CellType.STRING).setCellValue("日期");
                billHeadRow.createCell(1, CellType.STRING).setCellValue("星期");
                billHeadRow.createCell(2, CellType.STRING).setCellValue("开始时间");
                billHeadRow.createCell(3, CellType.STRING).setCellValue("结束时间");
                billHeadRow.createCell(4, CellType.STRING).setCellValue("费用");
                billHeadRow.createCell(5, CellType.STRING).setCellValue("是否分摊");

                for (int i = 0; i < stat.getBills().size(); i++) {
                    BookingBill bill = stat.getBills().get(i);
                    HSSFRow bsr = billSheet.createRow(i + 1);
                    bsr.createCell(0, CellType.STRING).setCellValue(Constant.FORMAT.format(bill.getDate()));
                    bsr.createCell(1, CellType.STRING).setCellValue(Constant.FORMAT_WEEK.format(bill.getDate()));
                    bsr.createCell(2, CellType.STRING).setCellValue(BookingTool.toTime(bill.getStart()));
                    bsr.createCell(3, CellType.STRING).setCellValue(BookingTool.toTime(bill.getEnd() + 1));
                    bsr.createCell(4, CellType.NUMERIC).setCellValue(bill.getFee());
                    bsr.createCell(5, CellType.STRING).setCellValue(bill.isShare() ? "Y" : "N");
                }
            }
            index++;
        }
        File dir = new File("bills");
        FileUtils.forceMkdir(dir);
        wb.write(new File(dir, Constant.FORMAT_MONTH.format(start) + ".xls"));
        wb.close();
    }

    private Collection<Statistic> calc(List<Booking> bookings, Date start) {
        Map<String, Statistic> mapping = new HashMap<>();
        for (Booking booking : bookings) {
            Statistic statistic = putIfAbsent(mapping, booking.getOwner(), start);
            statistic.setBookTimes(statistic.getBookTimes() + 1);
            statistic.setHours(statistic.getHours() + booking.getEnd() - booking.getStart() + 1);
            BookingBill bill = new BookingBill(
                    booking.getOpenId(),
                    booking.getId(),
                    booking.getFee(),
                    false,
                    booking.getDate(),
                    booking.getStart(),
                    booking.getEnd()
            );


            if (!CollectionUtils.isEmpty(booking.getShareUsers())) {
                int average = booking.getFee() / (booking.getShareUsers().size() + 1);
                bill.setFee(booking.getFee() - average * booking.getShareUsers().size());
                bill.setShare(true);
                for (User user : booking.getShareUsers()) {
                    Statistic ss = putIfAbsent(mapping, user, start);
                    ss.setFee(ss.getFee() + average);
                    ss.setHours(ss.getHours() + booking.getEnd() - booking.getStart() + 1);
                    ss.setBookTimes(ss.getBookTimes() + 1);
                    ss.getBills().add(
                            new BookingBill(ss.getUser().getOpenId(),
                                    booking.getId(),
                                    average,
                                    true,
                                    booking.getDate(),
                                    booking.getStart(),
                                    booking.getEnd()
                            ));
                }
            }
            statistic.getBills().add(bill);
            statistic.setFee(statistic.getFee() + bill.getFee());
        }
        return mapping.values();
    }

    private static Statistic putIfAbsent(Map<String, Statistic> map, User user, Date start) {
        return map.computeIfAbsent(user.getOpenId(), k -> {
            Statistic s = new Statistic();
            s.setUser(user);
            s.setMonth(start);
            return s;
        });
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
