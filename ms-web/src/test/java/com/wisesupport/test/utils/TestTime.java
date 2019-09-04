package com.wisesupport.test.utils;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * 功能描述
 *
 * @author c00286900
 * @since 2019-08-31
 */
public class TestTime {


    @Test
    public void testDuration(){

        Assert.assertTrue(Duration.parse("P0d").isZero());
        Assert.assertEquals(Duration.parse("P1d").toHours(),24);
        Assert.assertEquals(Duration.parse("PT1M").getSeconds(),60);
    }
}
