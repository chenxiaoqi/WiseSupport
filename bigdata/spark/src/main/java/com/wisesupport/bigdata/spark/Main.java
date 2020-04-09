package com.wisesupport.bigdata.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {

        SparkConf conf = new SparkConf().setMaster("local").setAppName("My Spark");
//        SparkConf conf = new SparkConf().setMaster("spark://vh-centos8:7077").setAppName("My Spark");
        JavaSparkContext sc = new JavaSparkContext(conf);
        

        Avg init = new Avg(0, 0);
        Avg result = sc.parallelize(Arrays.asList(1, 2, 3, 4), 2).aggregate(init, (x, y) -> {
            x.total = x.total + y;
            x.num = +x.num + 1;
            return x;
        }, (x, y) -> {
            x.total = x.total + y.total;
            x.num = x.num + y.num;
            return x;
        });
        System.out.println(result);
        sc.stop();
    }

    private static class Avg implements Serializable {
        int total;
        int num;

        public Avg(int total, int num) {
            this.total = total;
            this.num = num;
        }

        private void writeObject(ObjectOutputStream oo) throws IOException {
            oo.writeInt(total);
            oo.writeInt(num);
        }

        private void readObject(ObjectInputStream oo) throws IOException {
            total = oo.readInt();
            num = oo.readInt();
        }

        @Override
        public String toString() {
            return total + "=>" + num;
        }
    }
}
