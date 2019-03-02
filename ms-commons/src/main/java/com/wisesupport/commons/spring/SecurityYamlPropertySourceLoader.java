package com.wisesupport.commons.spring;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author chenxiaoqi on 2019-03-02.
 */
public class SecurityYamlPropertySourceLoader extends YamlPropertySourceLoader implements PropertySourceLoader {


    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {


        List<PropertySource<?>> result = new ArrayList<>();

        for (PropertySource<?> propertySource : super.load(name, resource)) {
            result.add(new PropertySource<PropertySource>(propertySource.getName(), propertySource) {
                @Override
                public Object getProperty(String name) {
                    Object obj = getSource().getProperty(name);
                    if (obj instanceof String) {
                        String value = (String) obj;
                        if (value.startsWith("ENC(") && value.endsWith(")")) {
                            obj = value.substring(4, value.length() - 1);
                        }
                    }
                    return obj;
                }
            });
        }
        return result;
    }
}
