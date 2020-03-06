package com.wisesupport.learn.algorithm;

import org.junit.Test;

import java.util.*;

public class BackTrace {

    @Test
    public void test() {
        System.out.println(permute(new int[]{1, 2, 3}));
        for (String str : generateParenthesis(2)) {
            System.out.println(str);
        }
        System.out.println(new HashSet<>(generateParenthesis(2)));
    }

    public List<String> generateParenthesis(int n) {
        char[] ans = new char[n * 2];
        List<String> result = new ArrayList<>();
        generateParenthesis(result, ans, 0, 0, 0, n);
        return result;
    }

    private void generateParenthesis(List<String> result, char[] ans, int index, int open, int close, int n) {
        if (index == n * 2) {
            result.add(new String(ans));
            return;
        }
        if (open < n) {
            ans[index] = '(';
            open++;
        } else if (close < n) {
            ans[index] = ')';
            close++;
        }
        generateParenthesis(result, ans, index + 1, open, close, n);
        if (ans[index] == '(') {
            ans[index] = ')';
            close++;
            open--;
        } else {
            ans[index] = '(';
            open++;
            close--;
        }
        generateParenthesis(result, ans, index + 1, open, close, n);

    }

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> output = new LinkedList<>();

        ArrayList<Integer> row = new ArrayList<>();
        for (int num : nums) {
            row.add(num);
        }
        int n = nums.length;
        backtrack(n, row, output, 0);
        return output;
    }

    public void backtrack(int n,
                          ArrayList<Integer> nums,
                          List<List<Integer>> output,
                          int first) {
        // if all integers are used up
        if (first == n) {
//            System.out.println();
            output.add(new ArrayList<>(nums));
        } else {
            for (int i = first; i < n; i++) {
                // place i-th integer first
                // in the current permutation
//                System.out.printf("%s<->%s ", first, i);
                Collections.swap(nums, first, i);
                // use next integers to complete the permutations
                backtrack(n, nums, output, first + 1);
                // backtrack
                Collections.swap(nums, first, i);
            }
        }
    }
}
