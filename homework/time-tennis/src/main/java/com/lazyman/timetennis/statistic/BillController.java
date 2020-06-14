package com.lazyman.timetennis.statistic;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
public class BillController {

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    private JdbcTemplate template;

    public BillController(JdbcTemplate template) {
        this.template = template;
    }

    @GetMapping("/stat/{month}")
    public List<Statistic> bill(@SessionAttribute User user, @PathVariable @DateTimeFormat(pattern = "yyyy-MM") Date month) {
        return getStatistics(month);
    }

    @GetMapping("/bills")
    public List<BookingBill> statDetail(@SessionAttribute User user, @RequestParam() @DateTimeFormat(pattern = "yyyy-MM") Date month, @RequestParam() @Size(max = 64) String openId) {
        return getBookingBills(month, openId);
    }

    @GetMapping(path = "/bills/export", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void export(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") Date month, HttpServletResponse response) throws IOException {
        String filename = Constant.FORMAT_MONTH.format(month) + ".xls";
        File file = new File("bills/" + filename);
        if (!file.exists()) {
            log.error("bill file not exists {}",file.getAbsoluteFile());
            response.setStatus(404);
            return;
        }
        response.addHeader("Content-Type", "application/vnd.ms-excel");
        response.addHeader("Content-Disposition", "attachment;filename=" + filename);
        try (FileInputStream fin = new FileInputStream(file)) {
            IOUtils.copy(fin, response.getOutputStream());
        }
        log.info("file downloaded");
    }

    private List<BookingBill> getBookingBills(Date month, String openId) {
        Date start = DateUtils.truncate(month, Calendar.MONTH);
        Date end = DateUtils.addMonths(start, 1);
        return template.query("select open_id,date,fee,share,start,end from booking_bill where open_id=? and date>=? and end<? order by date",
                new RowMapper<BookingBill>() {
                    @Override
                    public BookingBill mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        BookingBill bill = new BookingBill();
                        bill.setDate(rs.getDate("date"));
                        bill.setFee(rs.getInt("fee"));
                        bill.setStart(rs.getInt("start"));
                        bill.setEnd(rs.getInt("end"));
                        bill.setShare(rs.getBoolean("share"));
                        return bill;
                    }
                }, openId, start, end);
    }

    private List<Statistic> getStatistics(Date month) {
        Date start = DateUtils.truncate(month, Calendar.MONTH);
        return template.query("select a.open_id,a.month,a.fee,a.hours,a.book_times,a.balance,b.wx_nickname,b.nickname,b.avatar from monthly_stat a,tt_user b where a.open_id=b.open_id and a.month=?", new RowMapper<Statistic>() {
            @Override
            public Statistic mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                Statistic statistic = new Statistic();
                statistic.setOpenId(rs.getString("open_id"));
                statistic.setMonth(rs.getDate("month"));
                statistic.setFee(rs.getInt("fee"));
                statistic.setHours(rs.getInt("hours"));
                statistic.setBalance(rs.getInt("balance"));
                statistic.setBookTimes(rs.getInt("book_times"));
                User user = new User();
                user.setWxNickname(rs.getString("wx_nickname"));
                user.setNickname(rs.getString("nickname"));
                user.setAvatar(rs.getString("avatar"));
                statistic.setUser(user);
                return statistic;
            }
        }, start);
    }
}
