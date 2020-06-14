package com.lazyman.timetennis.user;

import org.apache.commons.lang3.Validate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ChargeService implements ApplicationContextAware {

    private UserMapper mapper;
    private JdbcTemplate template;
    private ApplicationContext context;

    public ChargeService(UserMapper userMapper, JdbcTemplate template) {
        this.mapper = userMapper;
        this.template = template;
    }

    public void charge(User operator, User target, int fee, int discountFee, String memo) {

        int balance = target.getBalance();
        if (mapper.charge(target.getOpenId(), discountFee) != 1) {
            throw new IllegalStateException("充值失败" + target.getOpenId());
        }
        target = mapper.selectByPrimaryKey(target.getOpenId());
        Validate.notNull(target);

        template.update("insert into charge_history (open_id, fee, memo) values(?,?,?)", target.getOpenId(), fee, memo);

        context.publishEvent(new BalanceEvent(this, operator, target, balance, fee, discountFee));
    }

    public void setApplicationContext(@NonNull ApplicationContext context) {
        this.context = context;
    }
}
