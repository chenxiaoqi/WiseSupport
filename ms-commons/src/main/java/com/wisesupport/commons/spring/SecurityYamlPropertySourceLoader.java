package com.wisesupport.commons.spring;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  * Author chenxiaoqi on 2019-03-02.
 *   */
public class SecurityYamlPropertySourceLoader extends YamlPropertySourceLoader implements PropertySourceLoader {


    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {


        List<PropertySource<?>> result = new ArrayList<>();


        for (PropertySource<?> propertySource : super.load(name, resource)) {

            for (Map.Entry<String, OriginTrackedValue> entry : ((Map<String, OriginTrackedValue>) propertySource.getSource()).entrySet()) {
                if (entry.getValue().getValue() instanceof String) {
                    String value = (String) entry.getValue().getValue();
                    if (value.startsWith("ENC(") && value.endsWith(")")) {
                        value = value.substring(4, value.length() - 1);
                    }
                    entry.setValue(OriginTrackedValue.of(value,entry.getValue().getOrigin()));
                }
            }
            result.add(propertySource);
        }
        return result;
    }
}

