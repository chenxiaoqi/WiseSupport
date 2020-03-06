package com.wisesupport.learn.algorithm;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicProgramming {

    @Test
    public void test() {
        System.out.println(mostValuable(5));
        System.out.println(mostValuable(7));
        Assert.assertThat(commonSubString("fish", "finish"), Matchers.is("ish"));
        Assert.assertThat(commonSubString("abc", "dbe"), Matchers.is("b"));
        Assert.assertThat(commonSubString("abc", "def"), Matchers.nullValue());
        Assert.assertThat(commonSubString("12345", "12312345666"), Matchers.is("12345"));


    }

    public String commonSubString(String str1, String str2) {
        int[][] grids = new int[str1.length()][str2.length()];
        int maxCount = 0;
        int hc = 0;
        for (int i = 0; i < str1.length(); i++) {
            for (int j = 0; j < str2.length(); j++) {
                if (str1.charAt(i) == str2.charAt(j)) {
                    if (i == 0 || j == 0) {
                        grids[i][j] = 1;
                    } else {
                        grids[i][j] = grids[i - 1][j - 1] + 1;
                    }
                    if (maxCount < grids[i][j]) {
                        maxCount = grids[i][j];
                        hc = i;
                    }
                }
            }
        }

        if (hc != 0) {
            return str1.substring(hc - maxCount + 1, hc + 1);
        } else {
            return null;
        }
    }

    private Cell mostValuable(int weight) {
        List<Good> goods = create();
        Cell[][] cells = new Cell[goods.size()][weight];
        for (int i = 0; i < goods.size(); i++) {
            Good good = goods.get(i);
            for (int j = 0; j < weight; j++) {
                cells[i][j] = new Cell();
                int price = 0;
                int remainWeight = j + 1 - good.getWeight();
                if (remainWeight >= 0) {
                    price = good.getPrice();
                }
                if (j != 0 && i != 0 && remainWeight > 0) {
                    remainWeight = j + 1 - good.getWeight();
                    if (remainWeight > 0) {
                        price = price + cells[i - 1][remainWeight - 1].getPrice();
                    }
                }

                Cell pre = null;
                if (i != 0) {
                    pre = cells[i - 1][j];
                }

                if (pre == null || pre.getPrice() < price) {
                    cells[i][j].add(good);
                    if (remainWeight > 0 && j != 0 && i != 0) {
                        cells[i][j].add(cells[i - 1][remainWeight - 1].getGoods());
                    }
                } else {
                    cells[i][j].add(pre.getGoods());
                }
            }
        }
        return cells[goods.size() - 1][weight - 1];
    }

    private List<Good> create() {
        return Arrays.asList(
                new Good("Guitar", 1500, 1),
                new Good("Laptop", 2000, 3),
                new Good("Sound", 3000, 4),
                new Good("iPhone", 2000, 2),
                new Good("Diamond", 4000, 3)
        );
    }

    private static class Cell {
        private List<Good> goods = new ArrayList<>();
        private int price;
        private int weight;

        public void add(Good good) {
            goods.add(good);
            price = price + good.getPrice();
            weight = weight + good.getWeight();
        }

        public void add(List<Good> goods) {
            for (Good good : goods) {
                add(good);
            }
        }

        public List<Good> getGoods() {
            return goods;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return goods + " => " + price + ' ' + weight;
        }
    }

    private static class Good {
        private String name;
        private int price;
        private int weight;

        public Good(String name, int price, int weight) {
            this.name = name;
            this.price = price;
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return name + " " + price + " " + weight;
        }
    }
}
