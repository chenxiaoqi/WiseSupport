package com.wisesupport.test.utils;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.junit.Test;

import java.util.Arrays;

public class GuavaTest {

    @Test
    public void test() {
        Table<Integer,Integer,Integer> table = ArrayTable.create(Arrays.asList(0, 1, 2, 3, 4, 5), Arrays.asList(0, 1, 2, 3, 4));
        table.put(2, 3, 2);
        System.out.println(table);
    }
}
