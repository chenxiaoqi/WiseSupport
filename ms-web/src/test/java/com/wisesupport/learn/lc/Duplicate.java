package com.wisesupport.learn.lc;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class Duplicate extends Base {
    @Test
    public void test() {
        System.out.println(intersect(new int[]{1, 1}, new int[]{1, 1, 1}));
    }

    public boolean containsDuplicate(int[] nums) {
        if (nums.length <= 1) {
            return false;
        }
        for (int i = 0; i < nums.length; i++) {
            int val = nums[i];
            for (int j = i + 1; j < nums.length; j++) {
                if (val == nums[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    public int singleNumber(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            int val = nums[i];
            boolean isSingle = true;
            for (int j = 0; j < nums.length; j++) {
                if (val == nums[j] && j != i) {
                    isSingle = false;
                    break;
                }
            }
            if (isSingle) {
                return val;
            }
        }
        return nums[nums.length - 1];
    }

    public int removeDuplicates(int[] nums) {
        if (nums.length <= 1) {
            return nums.length;
        }
        int temp = nums[0];
        int end = nums.length - 1;
        for (int position = 1; position <= end; ) {
            if (nums[position] == temp) {
                shift(nums, position);
                end--;
            } else {
                temp = nums[position];
                position++;
            }
        }
        return end + 1;
    }

    public int[] intersect(int[] nums1, int[] nums2) {
        if (nums1.length == 0 || nums2.length == 0) {
            return new int[0];
        }
        int[] found = new int[nums1.length];
        int length = nums2.length;
        int count = 0;
        for (int i = 0; i < nums1.length; i++) {
            int val = nums1[i];
            for (int j = 0; j < length; j++) {
                if (val == nums2[j]) {
                    found[count] = val;
                    shift(nums2, j);
                    length--;
                    count++;
                    break;
                }
            }
        }
        int[] result = new int[count];
        System.arraycopy(found, 0, result, 0, count);
        return result;
    }

    public int[] plusOne(int[] digits) {
        int start = digits.length - 1;
        do {
            if (digits[start] == 9) {
                digits[start] = 0;
                start--;
            } else {
                digits[start] = digits[start] + 1;
                break;
            }
        } while (start >= 0);
        if (start == -1) {
            int[] result = new int[digits.length + 1];
            result[0] = 1;
            System.arraycopy(digits, 0, result, 1, digits.length);
            return result;
        } else {
            return digits;
        }
    }

    public void moveZeroes(int[] nums) {
        if (nums.length <= 1) {
            return;
        }
        int zeros = 0;
        for (int num : nums) {
            if (num == 0) {
                zeros++;
            }
        }

        int position = 0;
        for (int num : nums) {
            if (num != 0) {
                nums[position] = num;
                position++;
            }
        }
        for (int i = 1; i <= zeros; i++) {
            nums[nums.length - i] = 0;
        }
    }

    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length - 1; i++) {
            int minus = target - nums[i];
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] == minus) {
                    result[0] = i;
                    result[1] = j;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isValidSudoku(char[][] board) {
        Set<Character> characters = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != '.') {
                    if (!characters.add(board[i][j])) {
                        return false;
                    }
                }
            }
            characters.clear();
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[j][i] != '.') {
                    if (!characters.add(board[j][i])) {
                        return false;
                    }
                }
            }
            characters.clear();
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int rowOffset = i * 3;
                int columnOffset = j * 3;
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        char c = board[rowOffset + k][columnOffset + l];
                        if (c != '.') {
                            if (!characters.add(c)) {
                                return false;
                            }
                        }
                    }
                }
                characters.clear();
            }
        }
        return true;
    }

    public void rotate(int[][] matrix) {
        int n = matrix.length;

        // transpose matrix
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int tmp = matrix[j][i];
                matrix[j][i] = matrix[i][j];
                matrix[i][j] = tmp;
            }
        }
        // reverse each row
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n / 2; j++) {
                int tmp = matrix[i][j];
                matrix[i][j] = matrix[i][n - j - 1];
                matrix[i][n - j - 1] = tmp;
            }
        }
    }
}
