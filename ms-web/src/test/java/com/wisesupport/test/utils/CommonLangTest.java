package com.wisesupport.test.utils;

import com.wisesupport.test.spring.TestBean;

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

/**
 * 功能描述
 *
 * @author c00286900
 * @since 2019-08-09
 */
public class CommonLangTest {

    /**
     *
     * @param param1 param1 desc
     * @return String
     */
    public String method(String param1) {
        return param1;
    }


    @Test
    public void diffBuilder(){
        TestBean t1 = new TestBean();
        t1.setCountry("china");
        TestBean t2 = new TestBean();
        DiffBuilder builder = new DiffBuilder(t1,t2, ToStringStyle.MULTI_LINE_STYLE,false);
        builder.append("country",t1.getCountry(),t2.getCountry());

        DiffResult result = builder.build();
        System.out.println(result.toString());
    }
}
