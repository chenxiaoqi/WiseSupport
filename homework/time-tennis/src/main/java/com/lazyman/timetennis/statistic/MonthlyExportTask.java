package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.ArenaDao;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingMapper;
import com.lazyman.timetennis.booking.QueryParam;
import com.lazyman.timetennis.booking.Share;
import com.lazyman.timetennis.menbership.MembershipCardBill;
import com.lazyman.timetennis.menbership.MembershipCardBillDao;
import com.lazyman.timetennis.wp.PayDao;
import com.lazyman.timetennis.wp.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 导出
 */
@Slf4j
public class MonthlyExportTask implements Runnable {
    private BookingMapper bookingMapper;
    private ArenaDao arenaDao;
    private PayDao payDao;
    private MembershipCardBillDao billDao;
    private String reportsDir;

    private Date today;
    private SXSSFWorkbook workbook;
    private long startTime;

    public MonthlyExportTask(BookingMapper bookingMapper, ArenaDao arenaDao, PayDao payDao, MembershipCardBillDao billDao, String reportsDir) {
        this.bookingMapper = bookingMapper;
        this.arenaDao = arenaDao;
        this.payDao = payDao;
        this.billDao = billDao;
        this.reportsDir = reportsDir;
    }

    @Transactional
    public void run() {
        Date end = DateUtils.truncate(getToday(), Calendar.MONTH);
        Date start = DateUtils.addMonths(end, -1);
        log.info("monthly export task {} ~ {}", Constant.FORMAT.format(start), Constant.FORMAT.format(end));
        List<Integer> arenaIds = arenaDao.arenaIds();
        for (Integer arenaId : arenaIds) {
            onStart();
            writeBooking(arenaId, start, end);
            onEnd(start, arenaId);
        }
    }

    private void onEnd(Date start, Integer arenaId) {

        try (FileOutputStream out = new FileOutputStream(new File(this.reportsDir, Constant.FORMAT_MONTH.format(start) + arenaId + ".xlsx"))) {
            workbook.write(out);
            workbook.close();
        } catch (IOException e) {
            log.error("write excel failed.", e);
        }
        log.info("export arena {},elapse {}s", arenaId, (System.currentTimeMillis() - this.startTime) / 1000);
    }

    private void writeBooking(Integer arenaId, Date start, Date end) {
        QueryParam param = new QueryParam();
        param.setStart(start);
        param.setEnd(end);
        param.setArenaId(arenaId);
        param.setCharged(true);
        param.setStatus("ok");
        List<Booking> bookings = bookingMapper.queryInDateRange(param);

        SXSSFSheet sheet = workbook.createSheet("订场记录");
        sheet.setColumnWidth(0, 256 * 20);
        SXSSFRow header = sheet.createRow(0);
        header.createCell(0, CellType.STRING).setCellValue("用户ID");
        header.createCell(1, CellType.STRING).setCellValue("用户昵称");
        header.createCell(2, CellType.STRING).setCellValue("预定场地");
        header.createCell(3, CellType.STRING).setCellValue("预定场次");
        header.createCell(4, CellType.STRING).setCellValue("预定时长(单位:半小时)");
        header.createCell(5, CellType.STRING).setCellValue("支付方式");
        header.createCell(6, CellType.STRING).setCellValue("订单编号");
        header.createCell(7, CellType.STRING).setCellValue("支付金额(单位:分)");
        int rowIndex = 1;
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            SXSSFRow row = sheet.createRow(rowIndex);
            rowIndex++;
            row.createCell(0, CellType.STRING).setCellValue(booking.getOwner().getOpenId());
            row.createCell(1, CellType.STRING).setCellValue(booking.getOwner().getWxNickname());
            row.createCell(2, CellType.STRING).setCellValue(booking.getCourt().getName());
            row.createCell(3, CellType.STRING).setCellValue(format(booking.getDate(), booking.getStart(), booking.getEnd()));
            row.createCell(4, CellType.STRING).setCellValue(booking.getEnd() - booking.getStart() + 1);
            row.createCell(5, CellType.STRING).setCellValue(booking.getPayType().equals("mc") ? "会员卡" : "微信支付");
            String tradeNo;
            int fee;
            if (booking.getPayType().equals("mc")) {
                MembershipCardBill bill = billDao.load(booking.getPayNo());
                tradeNo = booking.getPayNo();
                fee = bill.getFee();
            } else {
                Trade trade = payDao.load(booking.getPayNo());
                tradeNo = trade.getTransactionId();
                fee = trade.getFee();
            }
            row.createCell(6, CellType.STRING).setCellValue(tradeNo);
            row.createCell(7, CellType.STRING).setCellValue(fee);

            if (!CollectionUtils.isEmpty(booking.getShareUsers())) {
                for (Share share : booking.getShareUsers()) {
                    row = sheet.createRow(rowIndex);
                    rowIndex++;
                    row.createCell(0, CellType.STRING).setCellValue(share.getOpenId());
                    row.createCell(1, CellType.STRING).setCellValue(share.getWxNickname());
                    row.createCell(2, CellType.STRING).setCellValue(booking.getCourt().getName());
                    row.createCell(3, CellType.STRING).setCellValue(format(booking.getDate(), booking.getStart(), booking.getEnd()));
                    row.createCell(4, CellType.STRING).setCellValue(booking.getEnd() - booking.getStart() + 1);
                    row.createCell(5, CellType.STRING).setCellValue("会员卡");
                    MembershipCardBill bill = billDao.load(share.getPayNo());
                    tradeNo = booking.getPayNo();
                    fee = bill.getFee();
                    row.createCell(6, CellType.STRING).setCellValue(tradeNo);
                    row.createCell(7, CellType.STRING).setCellValue(fee);
                }
            }
        }
    }

    private String format(Date date, int start, int end) {
        return Constant.FORMAT.format(date) + " " + format(start) + "-" + format(end + 1);
    }

    private String format(int start) {
        return start / 2 + ":" + ((start & 1) == 0 ? "00" : "30");
    }

    private void onStart() {
        this.startTime = System.currentTimeMillis();
        workbook = new SXSSFWorkbook();
    }

    public void setToday(String today) {
        try {
            this.today = Constant.FORMAT.parse(today);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Date getToday() {
        return this.today == null ? new Date() : this.today;
    }
}
