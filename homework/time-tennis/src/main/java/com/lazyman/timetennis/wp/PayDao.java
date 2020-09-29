package com.lazyman.timetennis.wp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Component
public class PayDao {
    private int tradeExpireMinutes;

    private JdbcTemplate template;

    public PayDao(@Value("${wx.pay-expire-minutes}") int tradeExpireMinutes,
                  JdbcTemplate template) {
        this.tradeExpireMinutes = tradeExpireMinutes;
        this.template = template;
    }

    void createTrade(String tradeNo, String openId, String productType, String prepayId, int totalFee, String mchId) {
        template.update("insert into trade (trade_no, open_id, product_type, prepare_id, fee,mch_id) values (?,?,?,?,?,?)",
                tradeNo, openId, productType, prepayId, totalFee, mchId);

    }

    Trade load(String tradNo) {
        return template.queryForObject("select trade_no,mch_id,fee,status,prepare_id,open_id,product_type,create_time from trade where trade_no=?", (rs, rowNum) -> {
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
        return template.query("select trade_no,status,fee,prepare_id,mch_id,open_id,product_type,create_time from trade where status='wp' and create_time<date_add(now(),interval ? minute ) limit 1", rs -> {
            if (rs.next()) {
                Trade trade = new Trade();
                populateTrade(rs, trade);
                return trade;
            } else {
                return null;
            }
        }, -tradeExpireMinutes);

    }

    public boolean hasWaitForPay(String openId) {
        return Objects.requireNonNull(template.query("select 1 from trade where open_id=? and status='wp' limit 1", ResultSet::next, openId));
    }

    private void populateTrade(ResultSet rs, Trade trade) throws SQLException {
        trade.setTradeNo(rs.getString("trade_no"));
        trade.setFee(rs.getInt("fee"));
        trade.setStatus(rs.getString("status"));
        trade.setPrepareId(rs.getString("prepare_id"));
        trade.setOpenId(rs.getString("open_id"));
        trade.setMchId(rs.getString("mch_id"));
        trade.setProductType(rs.getString("product_type"));
        trade.setCreateTime(rs.getTimestamp("create_time"));
    }
}
