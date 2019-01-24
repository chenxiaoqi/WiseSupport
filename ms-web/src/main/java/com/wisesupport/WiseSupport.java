package com.wisesupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport")
@ImportResource("classpath:/spring/applicationContext.xml")
public class WiseSupport implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(WiseSupport.class, args);
    }
}
