package com.wisesupport.learn.lc;

import org.junit.Test;

import java.util.*;

public class UniquePath {


    @Test
    public void test() {
        System.out.println(uniquePaths(7, 3));
    }

    public int uniquePaths(int m, int n) {
        int[] pre = new int[n];
        Arrays.fill(pre,1);
        for (int i = 0; i < m-1; i++) {
            int cache = 1;
            for (int j = 1; j < n; j++) {
                pre[j-1] = cache;
                cache= pre[j-1] + pre[j];
            }
            pre[n-1] = cache;
        }
        return pre[n-1];
    }

    private int[][] cache = new int[101][101];
    public int uniquePaths2(int m, int n) {
        if (m == 1 || n == 1) {
            return 1;
        };
        if (cache[m][n] != 0) {
            return cache[m][n];
        }
        int paths = uniquePaths2(m - 1, n);
        paths = paths + uniquePaths2(m, n - 1);
        cache[m][n] = paths;
        return paths;
    }

    public int uniquePaths1(int m, int n) {
        if (m == 1 && n == 1) {
            return 1;
        }
        LinkedList<Coordinate> stack = new LinkedList<>();
        stack.push(new Coordinate(1, 1));
        int count = 0;
        while (!stack.isEmpty()) {
            Coordinate top = stack.peek();
            Coordinate next = top.next(m, n);
            if (next != null) {
                if (next.x == m && next.y == n) {
                    count++;
                    stack.pop();
                } else {
                    stack.push(next);
                }
            } else {
                stack.pop();
            }
        }


        return count;
    }


    static class Coordinate {
        int x;
        int y;
        Iterator<Coordinate> children;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coordinate next(int m, int n) {
            if (children == null) {
                List<Coordinate> list = new ArrayList<>(2);
                if (x < m) {
                    list.add(new Coordinate(x + 1, y));
                }
                if (y < n) {
                    list.add(new Coordinate(x, y + 1));
                }
                children = list.iterator();
            }
            return children.hasNext() ? children.next() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
