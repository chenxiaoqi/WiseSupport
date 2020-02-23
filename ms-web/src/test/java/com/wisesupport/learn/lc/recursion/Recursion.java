package com.wisesupport.learn.lc.recursion;

import org.junit.Test;

public class Recursion {
    @Test
    public void test(){
        System.out.println("climbStairs(n) = " + climbStairs(3));;
    }
    public int fib(int N) {
        if (N < 2) {
            return N;
        }
        return fib(N - 1) + fib(N - 2);
    }

    private int[] cache = new int[100];
    public int climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        if (cache[n] != 0) {
            return cache[n];
        }
        int value = climbStairs(n - 1) + climbStairs(n - 2);
        cache[n] = value;
        return value;

    }
}
