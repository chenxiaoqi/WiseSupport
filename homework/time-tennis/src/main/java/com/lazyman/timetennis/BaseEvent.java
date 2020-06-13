package com.lazyman.timetennis;

import com.lazyman.timetennis.user.User;
import org.springframework.context.ApplicationEvent;

public class BaseEvent extends ApplicationEvent {
    public static final String OP_BOOK = "b";
    public static final String OP_BOOK_CANCEL = "cb";
    public static final String OP_BOOK_SHARE = "sb";
    public static final String OP_CHARGE = "c";
    private String operationType;
    private User operator;

    public BaseEvent(Object source, User operator, String operationType) {
        super(source);
        this.operator = operator;
        this.operationType = operationType;
    }

    public String getOperationType() {
        return operationType;
    }

    public User getOperator() {
        return operator;
    }
}
