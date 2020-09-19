package com.lazyman.timetennis.wp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayDao {
    private JdbcTemplate template;

    public PayDao(JdbcTemplate template) {
        this.template = template;
    }

    public void createTrade(String tradeNo, String openId, String productType, String prepayId, int totalFee, int arenaId, List<Integer> bookingIds, String mchId) {
        template.update("insert into trade (trade_no, open_id, product_type, prepare_id, fee,mch_id) values (?,?,?,?,?,?)",
                tradeNo, openId, productType, prepayId, totalFee, mchId);
        for (int bookingId : bookingIds) {
            template.update("insert into trade_booking_r (trade_no, booking_id, arena_id) values (?,?,?)", tradeNo, bookingId, arenaId);
        }
    }

    public Trade load(String tradNo) {
        return template.queryForObject("select trade_no,mch_id,fee,status,prepare_id,open_id from trade where trade_no=?", (rs, rowNum) -> {
            Trade trade = new Trade();
            trade.setTradeNo(rs.getString("trade_no"));
            trade.setFee(rs.getInt("fee"));
            trade.setStatus(rs.getString("status"));
            trade.setPrepareId(rs.getString("prepare_id"));
            trade.setOpenId(rs.getString("open_id"));
            trade.setMchId(rs.getString("mch_id"));
            return trade;
        }, tradNo);
    }

    void updateStatus(String tradNo, String status, String transactionId) {
        template.update("update trade set status=?,transaction_id where trade_no=?", status, tradNo, transactionId);
    }
}
