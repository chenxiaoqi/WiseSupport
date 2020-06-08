package com.lazyman.timetennis;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.lazyman.timetennis", "com.wisesupport.commons"})
@ImportResource("classpath:/spring/applicationContext.xml")
@EnableScheduling
public class TimeTennis {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .registerShutdownHook(true)
                .sources(TimeTennis.class)
                .run(args);
    }
}
