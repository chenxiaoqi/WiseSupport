package com.wisesupport.learn.lc.array;

import com.wisesupport.learn.lc.Base;
import org.junit.Test;

import java.util.Arrays;

public class Rotate extends Base {

    @Test
    public void testRotate() {
        int[] prices = new int[]{1, 2, 3, 4, 5, 6};
        rotate3(prices, 4);
        System.out.println(Arrays.toString(prices));

    }

    public void rotate3(int[] nums, int k) {
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, nums.length - 1);
    }

    public void rotate(int[] nums, int k) {
        k = k % nums.length;
        for (int i = 0; i < k; i++) {
            int cur = nums[nums.length - 1];
            for (int j = 0; j < nums.length; j++) {
                int temp = nums[j];
                nums[j] = cur;
                cur = temp;
            }
        }
    }

    public void rotate2(int[] nums, int k) {
        k = k % nums.length;
        for (int start = 0, count = 0; count < nums.length; start++) {
            int forSwap = nums[start];
            int index = start;
            do {
                index = index + k;
                if (index > nums.length - 1) {
                    index = index - nums.length;
                }

                int temp = nums[index];
                nums[index] = forSwap;
                forSwap = temp;

                count++;
            } while (index != start);
        }
    }
}
