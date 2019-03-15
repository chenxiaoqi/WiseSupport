package com.wisesupport.test.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.*;

/**
 *  * @author c00286900
 *   */
public class TestBeanFactory {

    @Test
    public void test() {
        AnnotationConfigApplicationContext acp = new AnnotationConfigApplicationContext();
        acp.register(Config.class);
        acp.refresh();

        Config config = acp.getBean(Config.class);

        Assert.assertNotSame(config.service1().getTestBean(), config.service2().getTestBean());

        Assert.assertSame(config.testBeanSingleton(), config.testBeanSingleton());

    }

    @Configuration
    @EnableLoadTimeWeaving
    private static class Config {
        @Bean
        @Scope("prototype")
        public TestBean testBeanPrototype() {
            return new TestBean();
        }

        @Bean
        public TestBean testBeanSingleton() {
            return new TestBean();
        }

        @Bean
        public ClientService service1() {
            ClientService service = new ClientService();
            service.setTestBean(testBeanPrototype());
            return service;
        }

        @Bean
        public ClientService service2() {
            ClientService service = new ClientService();
            service.setTestBean(testBeanPrototype());
            return service;
        }
    }
}

