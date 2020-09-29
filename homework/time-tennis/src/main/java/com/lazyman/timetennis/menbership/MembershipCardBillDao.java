package com.lazyman.timetennis.menbership;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MembershipCardBillDao {
    private JdbcTemplate template;

    public MembershipCardBillDao(JdbcTemplate template) {
        this.template = template;
    }

    public void add(String tradeNo, String openId, String code, String productType, int fee, int balance) {
        template.update(
                "insert into membership_card_bill (bill_no,open_id, code, product_type, fee, balance) values (?,?,?,?,?,?)",
                tradeNo, openId, code, productType, fee, balance);
    }

    List<MembershipCardBill> userBill(String openId, String code) {
        return template.query("select bill_no, product_type, fee, balance,create_time from membership_card_bill where open_id=? and code=? order by create_time desc limit 20", (rs, rowNum) -> {
            MembershipCardBill bill = new MembershipCardBill();
            bill.setBillNo(rs.getString("bill_no"));
            bill.setProductType(rs.getString("product_type"));
            bill.setFee(rs.getInt("fee"));
            bill.setBalance(rs.getInt("balance"));
            bill.setCreateTime(rs.getTimestamp("create_time"));
            return bill;
        }, openId, code);
    }
}
