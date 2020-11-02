package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class MembershipCardService {
    private MembershipCardDao mcDao;

    private MembershipCardBillDao billDao;

    public MembershipCardService(MembershipCardDao mcDao, MembershipCardBillDao billDao) {
        this.mcDao = mcDao;
        this.billDao = billDao;
    }

    void create(MembershipCardMeta meta, String openId) {
        if (mcDao.hasMeta(openId, meta.getId())) {
            log.warn("user {} already has meta {},maybe duplicate notify", openId, meta.getId());
            return;
        }
        String code = StringUtils.leftPad(String.valueOf(meta.getId()), 5, '0');
        String maxCode = mcDao.maxMembershipCardCode(meta.getId());
        if (maxCode == null) {
            code = code + StringUtils.leftPad("1", 5, '0');
        } else {
            code = code + StringUtils.leftPad(String.valueOf(Integer.parseInt(maxCode.substring(5)) + 1), 5, '0');
        }
        mcDao.createMembershipCard(openId, meta, code, DateUtils.addMonths(new Date(), meta.getExtendMonth()));
    }

    public void refund(User user, String payNo) {
        MembershipCardBill bill = billDao.load(payNo);
        Validate.notNull(bill);
        int balance = mcDao.recharge(bill.getCode(), bill.getFee());
        String tradeNo = payNo + "-R";
        billDao.add(tradeNo, user.getOpenId(), bill.getCode(), Constant.PRODUCT_REFUND, bill.getFee(), balance, new Date());
    }

    public void charge(String tradeNo, String openId, int fee, String productType, MembershipCard card, boolean ignoreLowBalance, Date feeTime) {
        fee = fee * card.getMeta().getDiscount() / 100;
        int balance = card.getBalance();
        if (fee != 0) {
            balance = mcDao.chargeFee(card.getCode(), fee, ignoreLowBalance);
        }
        billDao.add(tradeNo, openId, card.getCode(), productType, fee, balance, feeTime);
    }
}
