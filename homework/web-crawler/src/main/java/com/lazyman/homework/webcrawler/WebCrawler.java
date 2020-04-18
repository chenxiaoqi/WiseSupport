package com.lazyman.homework.webcrawler;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebCrawler {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .registerShutdownHook(true)
                .sources(WebCrawler.class)
                .run(args);
    }
}
