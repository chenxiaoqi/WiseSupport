package com.lazyman.timetennis.wp;

import org.springframework.context.ApplicationEvent;

public class TradeEvent extends ApplicationEvent {
    private Trade trade;

    public TradeEvent(Object source, Trade trade) {
        super(source);
        this.trade = trade;
    }

    public Trade getTrade() {
        return trade;
    }
}
