package com.lazyman.timetennis;

import com.lazyman.timetennis.user.User;
import org.springframework.context.ApplicationEvent;

public class BaseEvent extends ApplicationEvent {
    public static final String OP_BOOK = "b";
    public static final String OP_BOOK_CANCEL = "cb";
    public static final String OP_BOOK_SHARE = "sb";
    private User operator;

    public BaseEvent(Object source, User operator) {
        super(source);
        this.operator = operator;
    }

    public User getOperator() {
        return operator;
    }
}
