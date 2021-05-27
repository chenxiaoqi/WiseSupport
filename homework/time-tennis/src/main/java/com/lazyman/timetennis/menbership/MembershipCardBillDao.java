package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MembershipCardBillDao {
    private JdbcTemplate template;

    public MembershipCardBillDao(JdbcTemplate template) {
        this.template = template;
    }

    public void add(String tradeNo, String openId, String code, String productType, int fee, int balance, Date feeTime) {
        template.update(
                "insert into membership_card_bill (bill_no,open_id, code, product_type, fee, balance,create_time) values (?,?,?,?,?,?,?)",
                tradeNo, openId, code, productType, fee, balance, feeTime);
    }

    List<MembershipCardBill> bills(String code, Date start, Date end) {
        String sql = "select bill_no, product_type, fee, balance,create_time from membership_card_bill where code=? and create_time>=? and create_time<?  order by create_time desc";
        return template.query(sql, (rs, rowNum) -> {
            MembershipCardBill bill = new MembershipCardBill();
            bill.setBillNo(rs.getString("bill_no"));
            bill.setProductType(rs.getString("product_type"));
            bill.setFee(rs.getInt("fee"));
            bill.setBalance(rs.getInt("balance"));
            bill.setCreateTime(rs.getTimestamp("create_time"));
            return bill;
        }, code, start, end);
    }

    public MembershipCardBill load(String billNo) {
        return template.queryForObject("select open_id,code,fee,create_time from membership_card_bill where bill_no=?", (rs, rowNum) -> {
            MembershipCardBill bill = new MembershipCardBill();
            bill.setCode(rs.getString("code"));
            bill.setFee(rs.getInt("fee"));
            bill.setCreateTime(rs.getTimestamp("create_time"));

            User user = new User();
            user.setOpenId(rs.getString("open_id"));
            bill.setUser(user);

            return bill;
        }, billNo);
    }
}
