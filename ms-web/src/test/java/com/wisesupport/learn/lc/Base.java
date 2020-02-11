package com.wisesupport.learn.lc;

public class Base {

    protected void reverse(int[] nums, int start, int end) {
        int times = (end - start + 1) / 2;
        for (int i = 0; i < times; i++) {
            int temp = nums[start + i];
            nums[start + i] = nums[end - i];
            nums[end - i] = temp;
        }
    }

    protected int shift(int[] nums, int position) {
        if (nums.length == 0) {
            return 0;
        } else {
            if (position != nums.length - 1) {
                System.arraycopy(nums, position + 1, nums, position, nums.length - position - 1);
            }
            return nums.length - 1;
        }


    }

}
