package com.wisesupport.learn.algorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Search {

    @Test
    public void testBinarySearch() {
        Assert.assertEquals(1,binary(2,1,2,3,4,5));
        Assert.assertEquals(0,binary(1,1,2,3,4,5));
        Assert.assertEquals(4,binary(5,1,2,3,4,5));
        Assert.assertEquals(-1,binary(10,1,2,3,4,5));
    }

    @Test
    public void testQuickSort() {
        Assert.assertEquals(Arrays.asList(1,2,3,4,5),quickSort(Arrays.asList(3,2,1,5,4)));
        Assert.assertEquals(Arrays.asList(1,2),quickSort(Arrays.asList(2,1)));
    }

    @Test
    public void testSimpleList() {
       SimpleLinkedList<Integer> list = new SimpleLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        for (Integer integer : list) {
            System.out.println(integer);
        }
    }



    private static List<Integer> quickSort(List<Integer> list) {
        if (list.size() < 2) {
            return list;
        }

        int pivot = list.size() / 2;
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i == pivot) {
                continue;
            }
            if (list.get(i) <= list.get(pivot)) {
                left.add(list.get(i));
            }else{
                right.add(list.get(i));
            }
        }
        List<Integer> result = new ArrayList<>(quickSort(left));
        result.add(list.get(pivot));
        result.addAll(quickSort(right));
        return result;
    }
    private static int binary(int guess, int... args) {
        int low = 0;
        int high = args.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int number = args[mid];
            if (number == guess) {
                return mid;
            } else if (guess < number) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return -1;
    }
}
