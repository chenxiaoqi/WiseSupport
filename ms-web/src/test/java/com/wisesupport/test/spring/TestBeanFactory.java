package com.wisesupport.test.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author c00286900
 */
public class TestBeanFactory {

    @Test
    public void test(){
        AnnotationConfigApplicationContext acp = new AnnotationConfigApplicationContext();
        acp.register(Config.class);
        acp.refresh();

        Config config = acp.getBean(Config.class);

        Assert.assertNotSame(config.service1().getTestBean(), config.service2().getTestBean());

        Assert.assertSame(config.testBeanSingleton(),config.testBeanSingleton());

    }
}
