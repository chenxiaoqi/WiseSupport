package com.wisesupport.learn.lc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matrix {

    @Test
    public void test() {
        int[][] matrix = new int[][]{
                {1, 2, 3, 10},
                {4, 5, 6, 11},
                {7, 8, 9, 12}
        };
        System.out.println(spiralOrder(matrix));
    }

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0) {
            return result;
        }
        int row = matrix.length;
        int col = matrix[0].length;
        int[] rdi = {0, 1, 0, -1};
        int[] cdi = {1, 0, -1, 0};
        int r = 0, c = 0;
        int di = 0;
        boolean[][] seen = new boolean[row][col];
        while (result.size() < row * col) {
            result.add(matrix[r][c]);
            seen[r][c] = true;
            if (r + rdi[di] >= row || r + rdi[di] < 0 || c + cdi[di] >= col || c + cdi[di] < 0 || seen[r + rdi[di]][c + cdi[di]]) {
                di = di + 1;
                if (di >= 4) {
                    di = 0;
                }
            }
            r = r + rdi[di];
            c = c + cdi[di];
        }
        return result;
    }


}
