package com.wisesupport.learn.lc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public class StockSolution {

    @Test
    public void testProfit() {

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            int[] prices = new int[]{random.nextInt(100), random.nextInt(100), random.nextInt(100), random.nextInt(100), random.nextInt(100)};
            int profit = maxProfit(prices);
            Assert.assertEquals(profit, maxProfit2(prices));
            Assert.assertEquals(profit, maxProfit3(prices));
        }
    }

    public int maxProfit3(int[] prices) {
        if (prices.length <= 1) {
            return 0;
        }

        int position = prices.length - 1;
        for (int i = 0; i < prices.length - 1; i++) {
            if (prices[i + 1] < prices[i]) {
                position = i;
                break;
            }
        }
        int[] remain = new int[prices.length - position - 1];
        System.arraycopy(prices, position + 1, remain, 0, remain.length);

        return (prices[position] - prices[0]) + maxProfit3(remain);
    }

    public int maxProfit2(int[] prices) {

        if (prices.length <= 1) {
            return 0;
        }
        int indexOfMax = 0;

        for (int i = 0; i < prices.length; i++) {
            if (prices[i] > prices[indexOfMax]) {
                indexOfMax = i;
            }
        }

        int indexOfMin = 0;
        for (int i = indexOfMax - 1; i > 0; i--) {
            if (prices[i] < prices[i - 1]) {
                indexOfMin = i;
                break;
            }
        }

        int[] left = new int[indexOfMin];
        if (left.length != 0) {
            System.arraycopy(prices, 0, left, 0, left.length);
        }

        int[] right = new int[prices.length - indexOfMax - 1];
        if (right.length != 0) {
            System.arraycopy(prices, indexOfMax + 1, right, 0, right.length);
        }

        return maxProfit2(left) + (prices[indexOfMax] - prices[indexOfMin]) + maxProfit2(right);
    }

    public int maxProfit(int[] prices) {
        int totalProfit = 0;
        for (int i = 0; i < prices.length - 1; i++) {
            int price = prices[i];
            int profit = prices[i + 1] - price;
            if (profit > 0) {
                totalProfit += profit;
            }
        }
        return totalProfit;
    }
}
