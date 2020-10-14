package com.lazyman.timetennis;

import com.lazyman.timetennis.booking.BaseBookingEvent;
import com.lazyman.timetennis.booking.Booking;
import com.lazyman.timetennis.booking.BookingTool;
import com.lazyman.timetennis.log.Operation;
import com.lazyman.timetennis.log.OperationMapper;
import com.lazyman.timetennis.statistic.MonthFeeEvent;
import com.lazyman.timetennis.user.BalanceEvent;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OperationListener {

    private OperationMapper mapper;
    private UserMapper userMapper;

    public OperationListener(OperationMapper mapper, UserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @EventListener(BaseBookingEvent.class)
    public void onBooking(BaseBookingEvent event) {
        User operator = event.getOperator();
        if (operator == null) {
            return;
        }
        Booking booking = event.getBooking();
        Operation operation = new Operation();
        operation.setOperatorId(operator.getOpenId());
        operation.setDescription(BookingTool.toDescription(booking));
        operation.setOperationType(event.getOperationType());
        if (!operator.getOpenId().equals(booking.getOpenId())) {
            User ou = userMapper.selectByPrimaryKey(booking.getOpenId());
            String name = ou != null ? '[' + ou.getWxNickname() + "] " : "[?]";
            operation.setDescription(name + operation.getDescription());
        }
        log.info("{} {} {} {}", operator.getOpenId(), operator.getWxNickname(), event.getOperationType(), operation.getDescription());
        save(operation);
    }

    private void save(Operation operation) {
        try {
            mapper.insert(operation);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @EventListener(BalanceEvent.class)
    public void onUser(BalanceEvent event) {
        User user = event.getOperator();
        Operation operation = new Operation();
        operation.setOperatorId(user.getOpenId());
        operation.setOperationType(event.getOperationType());
        if (event.getFee() <= 0) {
            operation.setDescription(String.format("[%s]原账户余额%s,扣除上月消费%s元后余额%s", event.getTarget().getWxNickname(), event.getBalance(), -event.getFee(), event.getTarget().getBalance()));
        } else {
            operation.setDescription(String.format("[%s]原账户余额%s,充值%s元,充值后账户余额%s", event.getTarget().getWxNickname(), event.getBalance(), event.getFee(), event.getTarget().getBalance()));
        }
        save(operation);
    }

    @EventListener(MonthFeeEvent.class)
    public void onMonthFee(MonthFeeEvent event) {
        User user = event.getOperator();
        Operation operation = new Operation();
        operation.setOperatorId(user.getOpenId());
        operation.setOperationType(event.getOperationType());
        operation.setDescription("月度账单已经生成");
        save(operation);
    }
}
