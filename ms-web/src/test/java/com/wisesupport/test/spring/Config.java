package com.wisesupport.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Scope;

/**
 * @author c00286900
 */
@Configuration
@EnableLoadTimeWeaving
public class Config {

    @Bean
    @Scope("prototype")
    public TestBean testBeanPrototype(){
        return new TestBean();
    }

    @Bean
    public TestBean testBeanSingleton(){
        return new TestBean();
    }

    @Bean
    public ClientService service1(){
        ClientService service = new ClientService();
        service.setTestBean(testBeanPrototype());
        return service;
    }

    @Bean
    public ClientService service2(){
        ClientService service = new ClientService();
        service.setTestBean(testBeanPrototype());
        return service;
    }

}
