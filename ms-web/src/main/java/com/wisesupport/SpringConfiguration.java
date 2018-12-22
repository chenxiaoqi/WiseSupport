package com.huawei.mysupport.spring;

import java.net.URL;

import org.apache.catalina.Context;
import org.apache.commons.lang3.Validate;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.huawei.dcs.dcsdk.core.DCSDKCheckException;
import com.huawei.dcs.dcsdk.core.DCSDKFacade;
import com.huawei.dcs.dcsdk.core.IDCSOperation;
import com.huawei.mysupport.Application;
import com.huawei.mysupport.common.DCSConfigProvider;
import com.huawei.mysupport.common.EncryptionComponent;
import com.huawei.security.validator.filter.ParamCheckFilter;
import com.huawei.wisesupport.commons.SnowflakeIdWorker;


@Configuration
public class SpringConfiguration
{

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer()
    {
        return jacksonObjectMapperBuilder -> {
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            jacksonObjectMapperBuilder.modulesToInstall(simpleModule);
        };
    }

    @Bean
    public LocaleResolver localeResolver()
    {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("lang");
        resolver.setCookieSecure(true);
        resolver.setCookiePath("/");
        resolver.setCookieHttpOnly(true);
        return resolver;
    }

   
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor()
    {
        return new MethodValidationPostProcessor();
    }
    
}
