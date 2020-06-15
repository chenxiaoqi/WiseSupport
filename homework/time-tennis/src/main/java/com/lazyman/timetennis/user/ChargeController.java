package com.lazyman.timetennis.user;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.SessionWatch;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@RestController
public class ChargeController implements ApplicationContextAware {

    private String accountant;

    private UserMapper mapper;
    private ApplicationContext application;
    private JdbcTemplate template;

    public ChargeController(@Value("${wx.accountant}") String accountant, UserMapper mapper, JdbcTemplate template) {
        this.accountant = accountant;
        this.mapper = mapper;
        this.template = template;
    }

    @PostMapping("/charge")
    @Transactional
    public void charge(@SessionAttribute("user") User user,
                       @RequestParam @Size(min = 6, max = 64) String openId,
                       @Min(200) int fee, @Size(max = 64) String memo, @RequestParam(defaultValue = "true") boolean hasDiscount) {
        if (!user.getOpenId().equals(accountant)) {
            throw new BusinessException("没有权限");
        }
        User target = mapper.selectByPrimaryKey(openId);
        Validate.notNull(target);
        if (!target.getVip()) {
            throw new BusinessException("该用户还不是会员,请先开通会员");
        }

        int discountFee;
        if (hasDiscount) {
            if (fee == 200) {
                discountFee = 260;
            } else if (fee == 500) {
                discountFee = 750;
            } else if (fee == 1000) {
                discountFee = 1500;
            } else {
                throw new BusinessException("充值费用只能是,200,500,1000");
            }
        } else {
            discountFee = fee;
        }
        int balance = target.getBalance();
        if (mapper.charge(openId, discountFee) != 1) {
            throw new IllegalStateException("充值失败" + target.getOpenId());
        }
        target = mapper.selectByPrimaryKey(openId);
        Validate.notNull(target);

        template.update("insert into charge_history (open_id, fee, memo) values(?,?,?)", openId, fee, memo);

        application.publishEvent(new BalanceEvent(this, user, target, balance, fee, discountFee));

        SessionWatch.destroy(openId);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.application = applicationContext;
    }
}
