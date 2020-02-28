package com.wisesupport.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamTest {

    @Test
    public void test() {
        System.out.println(Arrays.toString(Stream.of(1, 2, 3, 4).mapToInt(Integer::intValue).toArray()));
        System.out.println(Stream.of(1, 2, 3).limit(1).collect(Collectors.toSet()));
        System.out.println(Stream.of(1, 2, 3).findAny());
        //peek is for debug
        System.out.println(Stream.of(3, 2, 1).peek(a -> {
        }).collect(Collectors.toList()));

        //取最大
        Stream.of(1, 5, 3).reduce((s, b) -> s.compareTo(b) > 0 ? s : b).ifPresent(System.out::println);

        //求和
        Stream.of(1, 2, 3).reduce(Integer::sum).ifPresent(System.out::println);

        //初始值求和
        System.out.println(Stream.of(1, 2, 3).reduce(100, Integer::sum));


    }

    @Test
    public void testFlat() {
        System.out.println(Arrays.toString(Stream.of(new int[]{1, 2, 3}, new int[]{4, 5, 6}).flatMapToInt(Arrays::stream).toArray()));
        System.out.println(Arrays.toString(Stream.of(new Integer[]{1, 2, 3}, new Integer[]{4, 5, 6}).flatMap(Arrays::stream).toArray()));
    }

    @Test
    public void testCollect() {
        System.out.println(Stream.of(1, 2, 3, 4).collect(ArrayList::new, ArrayList::add, ArrayList::addAll).toString());

    }


}
