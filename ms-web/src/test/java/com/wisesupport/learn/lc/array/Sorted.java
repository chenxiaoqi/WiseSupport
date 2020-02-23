package com.wisesupport.learn.lc.array;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;


public class Sorted {

    @Test
    public void test() {
        Assert.assertThat(merge(new int[]{2, 4}, new int[]{1, 3, 5, 6}), is(Arrays.asList(1, 2, 3, 4, 5, 6)));
        System.out.println(threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
        Assert.assertThat(threeSumClosest(new int[]{0, 0, 0}, 1), is(0));
        Assert.assertThat(maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}), is(49));
    }

    public int[] productExceptSelf(int[] nums) {
        if (nums.length == 1) {
            return nums;
        }
        int[] result = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            int total = 1;
            for (int j = 0; j < nums.length; j++) {
                if (j == i) {
                    continue;
                }
                total = total * nums[j];
            }
            result[i] = total;
        }
        return result;
    }

    public int maxArea(int[] height) {
        int max = 0;
        int l = 0;
        int r = height.length - 1;
        while (l < r) {
            max = Math.max((r - l) * Math.min(height[l], height[r]), max);
            if (height[l] <= height[r]) {
                l++;
            } else {
                r--;
            }
        }
        return max;
    }

    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        int result = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length - 2; i++) {
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];
                if (Math.abs(target - sum) < Math.abs(target - result)) {
                    result = sum;
                }
                if (sum > target) {
                    r--;
                } else if (sum < target) {
                    l++;
                } else {
                    return sum;
                }
            }
        }
        return result;
    }

    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (nums[i] > 0) {
                break;
            }
            if (i != 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];
                if (sum == 0) {
                    List<Integer> tuple = new ArrayList<>();
                    tuple.add(nums[i]);
                    tuple.add(nums[l]);
                    tuple.add(nums[r]);
                    result.add(tuple);
                }
                if (sum > 0) {
                    r--;
                    while (r > l && nums[r] == nums[r + 1]) {
                        r--;
                    }
                } else {
                    l++;
                    while (l < r && nums[l] == nums[l - 1]) {
                        l++;
                    }
                }
            }
        }
        return result;
    }

    public List<Integer> merge(int[] left, int[] right) {
        List<Integer> result = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;
        while (rightIndex < right.length) {
            if (leftIndex >= left.length) {
                result.add(right[rightIndex]);
                rightIndex++;
            } else if (right[rightIndex] <= left[leftIndex]) {
                result.add(right[rightIndex]);
                rightIndex++;
            } else {
                result.add(left[leftIndex]);
                leftIndex++;
            }
        }
        return result;
    }
}
