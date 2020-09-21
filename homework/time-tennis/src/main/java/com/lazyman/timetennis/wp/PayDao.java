package com.lazyman.timetennis.wp;

import com.lazyman.timetennis.booking.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class PayDao {
    private int tradeExpireMinutes;

    private JdbcTemplate template;

    public PayDao(@Value("${wx.pay-expire-minutes}") int tradeExpireMinutes,
                  JdbcTemplate template) {
        this.tradeExpireMinutes = tradeExpireMinutes;
        this.template = template;
    }

    public void createTrade(String tradeNo, String openId, String productType, String prepayId, int totalFee, int arenaId, List<Booking> bookings, String mchId) {
        template.update("insert into trade (trade_no, open_id, product_type, prepare_id, fee,mch_id) values (?,?,?,?,?,?)",
                tradeNo, openId, productType, prepayId, totalFee, mchId);
        for (Booking booking : bookings) {
            template.update("insert into trade_booking_r (trade_no, booking_id, arena_id,court_id,start,end,date) values (?,?,?,?,?,?,?)",
                    tradeNo, booking.getId(), arenaId, booking.getCourt().getId(), booking.getStart(), booking.getEnd(), booking.getDate());
        }
    }

    public void deleteTradeBooking(String tradeNo) {
        //todo 删除share表?
        template.update("delete from tt_booking where id in(select b.booking_id from trade a,trade_booking_r b where a.trade_no=b.trade_no and a.trade_no=?)", tradeNo);
    }

    public Trade load(String tradNo) {
        return template.queryForObject("select trade_no,mch_id,fee,status,prepare_id,open_id from trade where trade_no=?", (rs, rowNum) -> {
            Trade trade = new Trade();
            populateTrade(rs, trade);
            return trade;
        }, tradNo);
    }

    void updateStatus(String tradNo, String status, String transactionId) {
        template.update("update trade set status=?,transaction_id=? where trade_no=?", status, transactionId, tradNo);
    }

    public void updateStatus(String tradNo, String status) {
        template.update("update trade set status=? where trade_no=?", status, tradNo);
    }

    public Trade pollWaitForPay() {
        return template.query("select trade_no,status,fee,prepare_id,mch_id,open_id from trade where status='wp' and create_time<date_add(now(),interval ? minute ) limit 1", rs -> {
            if (rs.next()) {
                Trade trade = new Trade();
                populateTrade(rs, trade);
                return trade;
            } else {
                return null;
            }
        }, -tradeExpireMinutes);

    }

    private void populateTrade(ResultSet rs, Trade trade) throws SQLException {
        trade.setTradeNo(rs.getString("trade_no"));
        trade.setFee(rs.getInt("fee"));
        trade.setStatus(rs.getString("status"));
        trade.setPrepareId(rs.getString("prepare_id"));
        trade.setOpenId(rs.getString("open_id"));
        trade.setMchId(rs.getString("mch_id"));
    }
}
