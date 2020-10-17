package com.lazyman.timetennis.statistic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
public class StatisticDao {
    private JdbcTemplate template;

    public StatisticDao(JdbcTemplate template) {
        this.template = template;
    }


    public int getCancelTimes(String openId, Date date) {
        Integer count = template.query("select cancel_times from monthly_stat where month=? and open_id=?", rs -> {
            if (rs.next()) {
                return rs.getInt("cancel_times");
            } else {
                return 0;
            }
        }, date, openId);
        return Objects.requireNonNull(count);
    }

    public void setCancelTimes(String openId, Date date, int times) {
        template.update("replace into monthly_stat(month,open_id,cancel_times) values(?,?,?)", date, openId, times);
    }
}
