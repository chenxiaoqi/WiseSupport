package com.wisesupport.cg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author chenxiaoqi on 2019-01-23.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport.cg")
public class CodeGenerator {
    public static void main(String[] args) {
        SpringApplication.run(CodeGenerator.class, args);
    }
}
