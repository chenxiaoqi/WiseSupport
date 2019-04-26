package com.wisesupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * * Author chenxiaoqi on 2018/12/22.
 */

@SpringBootApplication(scanBasePackages = "com.wisesupport")
@ImportResource("classpath:/spring/applicationContext.xml")
@EnableCaching
public class WiseSupport {

    private static final Logger LOG = LoggerFactory.getLogger(WiseSupport.class);

    @Bean
    public TaskExecutorCustomizer executorCustomizer() {
        return taskExecutor -> taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer() {
        return (schedulerFactoryBean) -> {
            Properties properties = new Properties();
            properties.setProperty("org.quartz.jobStore.isClustered", "true");
            properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
            schedulerFactoryBean.setQuartzProperties(properties);
        };
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(AsyncTaskExecutor taskExecutor) {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.registerCallableInterceptors(new CallableProcessingInterceptor() {
                    @Override
                    public <T> void afterCompletion(NativeWebRequest request, Callable<T> task) {
                        LOG.debug("async call completed.");
                    }
                }).setTaskExecutor(taskExecutor);
            }
        };
    }


    public static void main(String[] args) {
        ClassPathXmlApplicationContext parent = new ClassPathXmlApplicationContext("/spring/applicationContext-parent.xml");
        new SpringApplicationBuilder()
                .parent(parent)
                .registerShutdownHook(true)
                .sources(WiseSupport.class)
                .run(args);

    }
}

