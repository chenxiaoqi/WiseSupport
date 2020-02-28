package com.wisesupport.learn.algorithm;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicProgramming {

    @Test
    public void test() {
        System.out.println(mostValuable(5));
        System.out.println(mostValuable(7));
    }

    private Cell mostValuable(int weight) {
        List<Good> goods = create();
        Cell[][] cells = new Cell[goods.size() + 1][weight + 1];
        for (int i = 0; i <= goods.size(); i++) {
            for (int j = 0; j <= weight; j++) {
                cells[i][j] = new Cell();
            }
        }
        for (int i = 1; i <= goods.size(); i++) {
            Good good = goods.get(i - 1);
            for (int j = 1; j <= weight; j++) {
                if (good.getWeight() > j) {
                    cells[i][j].add(cells[i - 1][j].getGoods());
                } else {
                    if (good.getPrice() + cells[i - 1][j - good.getWeight()].getValue() > cells[i - 1][j].getValue()) {
                        cells[i][j].add(good);
                        cells[i][j].add(cells[i - 1][j - good.getWeight()].getGoods());
                    } else {
                        cells[i][j].add(cells[i - 1][j].getGoods());
                    }
                }
            }
        }
        return cells[goods.size()][weight];
    }

    private List<Good> create() {
        return Arrays.asList(
                new Good("Guitar", 1500, 1),
                new Good("Laptop", 2000, 3),
                new Good("Sound", 3000, 4),
                new Good("iPhone", 2000, 1)
        );
    }

    private static class Cell {
        private List<Good> goods = new ArrayList<>();
        private int value;

        public void add(Good good) {
            goods.add(good);
            value = value + good.getPrice();
        }

        public void add(List<Good> goods) {
            for (Good good : goods) {
                add(good);
            }
        }

        public List<Good> getGoods() {
            return goods;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return goods + " => " + value;
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
