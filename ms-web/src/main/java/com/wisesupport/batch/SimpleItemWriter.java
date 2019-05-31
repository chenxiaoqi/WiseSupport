package com.wisesupport.batch;

import com.wisesupport.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import java.util.List;

@Slf4j
public class SimpleItemWriter implements ItemWriter<User> {

    @Override
    public void write(@NonNull List<? extends User> items) {
        log.info("write {}", items);

    }
}
