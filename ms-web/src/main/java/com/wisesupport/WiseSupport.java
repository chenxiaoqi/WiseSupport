package com.wisesupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.configuration.Swagger2DocumentationConfiguration;

/**
 * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport")
@ImportResource("/spring/applicationContext.xml")
@Import(Swagger2DocumentationConfiguration.class)
public class WiseSupport {

    public static void main(String[] args) {
        SpringApplication.run(WiseSupport.class, args);
    }
}
