package com.wisesupport.batch;

import com.wisesupport.user.User;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.ListItemReader;

import java.util.Arrays;

public class SimpleItemReader implements ItemReader<User> {

    private final ItemReader<User> itemReader;

    public SimpleItemReader() {
        itemReader = new ListItemReader<>(Arrays.asList(new User("cxq"), new User("apollo")));
    }

    @Override
    public User read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return itemReader.read();
    }
}
