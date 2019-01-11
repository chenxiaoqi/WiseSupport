package com.wisesupport.commons.spring;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Author chenxiaoqi on 2019-01-11.
 */
public class Jackson2ObjectMapperBuilderCustomizerImpl implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        jacksonObjectMapperBuilder.modulesToInstall(simpleModule);

    }
}
