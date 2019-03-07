package com.wisesupport.test.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author c00286900
 * @version [版本号, 2019/2/21]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Lion extends Animal {

    @JsonCreator
    public Lion(@JsonProperty("name") String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Lion: " + getName();
    }

}
