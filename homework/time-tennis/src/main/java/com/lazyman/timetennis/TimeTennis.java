package com.lazyman.timetennis;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.lazyman.timetennis", "com.wisesupport.commons"})
@ImportResource("classpath:/spring/applicationContext.xml")
@PropertySource("classpath:password.properties")
@ServletComponentScan(basePackageClasses = TimeTennis.class)
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
public class TimeTennis {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .registerShutdownHook(true)
                .sources(TimeTennis.class)
                .run(args).registerShutdownHook();
    }
}
