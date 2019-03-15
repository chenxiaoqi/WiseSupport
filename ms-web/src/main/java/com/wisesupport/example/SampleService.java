package com.wisesupport.example;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  * @author c00286900
 *   */
@Component
public class SampleService {

    @Cacheable("sample.expensive")
    public String expensive(int i) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date()) + "=>" + i;
    }
}

