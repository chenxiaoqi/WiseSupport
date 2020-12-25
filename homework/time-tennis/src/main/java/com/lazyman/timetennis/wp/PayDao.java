package com.lazyman.timetennis.wp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Component
public class PayDao {
    private final int tradeExpireMinutes;

    private final JdbcTemplate template;

    public PayDao(@Value("${wx.pay-expire-minutes}") int tradeExpireMinutes,
                  JdbcTemplate template) {
        this.tradeExpireMinutes = tradeExpireMinutes;
        this.template = template;
    }

    void createTrade(String tradeNo, String openId, String productType, String prepayId, int totalFee) {
        this.createTrade(tradeNo, openId, productType, prepayId, totalFee, null, null);
    }

    void createTrade(String tradeNo, String openId, String productType, String prepayId, int totalFee, String receiverId, Integer receiverType) {
        template.update("insert into trade (trade_no, open_id, product_type, prepare_id, fee,receiver_id,receiver_type) values (?,?,?,?,?,?,?)",
                tradeNo, openId, productType, prepayId, totalFee, receiverId, receiverType);

    }

    public Trade load(String tradNo) {
        return template.queryForObject("select trade_no,receiver_id,fee,status,prepare_id,transaction_id,open_id,product_type,create_time,receiver_id,receiver_type,share_status from trade where trade_no=?", (rs, rowNum) -> {
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
        return template.query("select trade_no,status,fee,prepare_id,transaction_id,open_id,product_type,create_time,receiver_id,receiver_type,share_status from trade where status='wp' and create_time<date_add(now(),interval ? minute ) limit 1", rs -> {
            if (rs.next()) {
                Trade trade = new Trade();
                populateTrade(rs, trade);
                return trade;
            } else {
                return null;
            }
        }, -tradeExpireMinutes);

    }

    public Trade pollWaitForShare() {
        return template.query("select trade_no,status,fee,prepare_id,transaction_id,open_id,product_type,create_time,receiver_id,receiver_type,share_status from trade where share_status='wfs' limit 1", rs -> {
            if (rs.next()) {
                Trade trade = new Trade();
                populateTrade(rs, trade);
                return trade;
            } else {
                return null;
            }
        });
    }

    public void updateShareStatus(String tradeNo, String shareStatus) {
        template.update("update trade set share_status=? where trade_no=?", shareStatus, tradeNo);
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
        trade.setReceiverId(rs.getString("receiver_id"));
        trade.setProductType(rs.getString("product_type"));
        trade.setCreateTime(rs.getTimestamp("create_time"));
        trade.setTransactionId(rs.getString("transaction_id"));
        trade.setReceiverId(rs.getString("receiver_id"));
        trade.setReceiverType(rs.getObject("receiver_type", Integer.class));
        trade.setShareStatus(rs.getString("share_status"));
    }
}
