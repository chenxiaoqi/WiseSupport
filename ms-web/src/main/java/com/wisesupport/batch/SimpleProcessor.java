package com.wisesupport.batch;

import com.wisesupport.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

@Slf4j
public class SimpleProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(@NonNull User item) throws InterruptedException {
        log.info("process {}", item.getAccount());
        Thread.sleep(10000);
        item.setAccount("hi " + item.getAccount());
        return item;
    }
}
