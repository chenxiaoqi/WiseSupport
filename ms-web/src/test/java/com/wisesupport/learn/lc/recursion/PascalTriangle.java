package com.wisesupport.learn.lc.recursion;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PascalTriangle {
    @Test
    public void test() {
        System.out.println(generate(5));
        System.out.println(getRow(3));
    }

    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 1; i <= numRows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                if (j == 0 || j == i - 1) {
                    row.add(1);
                } else {
                    row.add(result.get(i - 2).get(j - 1) + result.get(i - 2).get(j));
                }
            }
            result.add(row);
        }
        return result;
    }


    public List<Integer> getRow(int rowIndex) {
        rowIndex++;
        List<Integer> row = new ArrayList<>();
        int[][] cache = new int[rowIndex][rowIndex];
        for (int i = 0; i < rowIndex; i++) {
            if (i == 0 || i == rowIndex - 1) {
                row.add(1);
            } else {
                row.add(getValue(rowIndex, i,cache));
            }
        }
        return row;
    }

    private Integer getValue(int rowIndex, int i, int[][] cache) {
        if (i == 0 || i == rowIndex - 1) {
            return 1;
        }
        if (cache[rowIndex - 1][i] != 0) {
            return cache[rowIndex - 1][i];
        }
        int value =  getValue(rowIndex - 1, i - 1, cache) + getValue(rowIndex - 1, i, cache);
        cache[rowIndex - 1][i] = value;
        return value;
    }

}
