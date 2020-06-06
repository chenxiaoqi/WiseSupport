package com.lazyman.timetennis;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeTennis {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .registerShutdownHook(true)
                .sources(TimeTennis.class)
                .run(args);
    }
}
