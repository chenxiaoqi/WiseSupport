package com.wisesupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport")
@ImportResource("/spring/applicationContext.xml")
@EnableJdbcRepositories(basePackages = "com.wisesupport")
public class WiseSupport {

    public static void main(String[] args) {
        SpringApplication.run(WiseSupport.class, args);
    }
}
