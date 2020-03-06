package com.wisesupport.learn.lc;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class Str {

    @Test
    public void test() {

        Assert.assertThat(longestPalindrome("ababababa"), Matchers.is("ababababa"));
        Assert.assertThat(longestCommonPrefix(new String[]{"fly", "flow", "flight"}), Matchers.is("fl"));
        Assert.assertThat(isValid("{[[(())]]}"), Matchers.is(true));
        Assert.assertThat(isValid("{[[(()]]}"), Matchers.is(false));
        System.out.println(multiply("123", "2"));
    }

    public String reverseWords(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        StringBuilder builder = new StringBuilder();
        for (StringTokenizer stringTokenizer = new StringTokenizer(s, " "); stringTokenizer.hasMoreTokens(); ) {
            String nextToken = stringTokenizer.nextToken();
            for (int i = nextToken.length() - 1; i >= 0; i--) {
                builder.append(nextToken.charAt(i));
            }
            builder.append(' ');
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public String multiply(String num1, String num2) {
        int[] result = new int[num1.length() + num2.length()];
        for (int i = num1.length() - 1; i >= 0; i--) {
            int carry = 0;
            for (int j = num2.length() - 1; j >= 0; j--) {
                int n1 = num1.charAt(i) - '0';
                int n2 = num2.charAt(j) - '0';
                int rIdx = result.length - (num2.length() - j - 1) - (num1.length() - 1 - i) - 1;
                int mul = n1 * n2 + carry + result[rIdx];
                carry = mul / 10;
                result[rIdx] = mul % 10;
            }
            result[result.length - num2.length() - (num1.length() - 1 - i) - 1] = carry;
        }
        StringBuilder builder = new StringBuilder();
        boolean skip = true;
        for (int i : result) {
            if (i == 0) {
                if (skip) {
                    continue;
                }
            } else {
                skip = false;
            }
            builder.append(i);
        }
        return builder.length() == 0 ? "0" : builder.toString();
    }

    public boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        Stack<Character> stack = new Stack<>();
        try {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '{' || c == '(' || c == '[') {
                    stack.push(c);
                } else {
                    char top = stack.pop();
                    if (c == '}' && top != '{') {
                        return false;
                    } else if (c == ')' && top != '(') {
                        return false;
                    } else if (c == ']' && top != '[') {
                        return false;
                    }
                }
            }
        } catch (EmptyStackException e) {
            return false;
        }
        return stack.isEmpty();
    }

    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 1) {
            return strs[0];
        }
        int minLen = -1;
        for (String str : strs) {
            if (minLen == -1) {
                minLen = str.length();
            } else {
                minLen = Math.min(minLen, str == null ? 0 : str.length());
            }
        }
        if (minLen <= 0) {
            return "";
        }
        int position = 0;
        boolean same = true;
        for (; position < minLen; position++) {
            char letter = '0';
            for (String str : strs) {
                if (letter == '0') {
                    letter = str.charAt(position);
                } else {
                    if (str.charAt(position) != letter) {
                        same = false;
                        break;
                    }
                }
            }
            if (!same) {
                break;
            }
        }
        return strs[0].substring(0, position);
    }

    public String longestPalindrome(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }

        int start = 0, end = 0;
        int maxLen = 0;

        for (int i = 0; i < s.length(); i++) {
            int len1 = findPalindrome(s, i, i);
            int len2 = findPalindrome(s, i, i + 1);
            int len = Math.max(len1, len2);
            if (len > maxLen) {
                start = i - (len - 1) / 2;
                end = i + len / 2;
                maxLen = len;
            }
        }
        return s.substring(start, end + 1);
    }

    private int findPalindrome(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return right - left - 1;
    }


}
