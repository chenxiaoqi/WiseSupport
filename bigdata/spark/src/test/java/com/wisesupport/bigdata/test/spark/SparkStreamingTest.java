package com.wisesupport.bigdata.test.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Tuple2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SparkStreamingTest {

    @BeforeClass
    public static void beforeClass() throws IOException {
        final ServerSocket server = new ServerSocket(18080);
        Runnable runnable = () -> {
            try {
                while (true) {
                    Socket socket = server.accept();
                    new Thread(() -> {
                        try {
                            PrintStream ps = new PrintStream(socket.getOutputStream());
                            SimpleDateFormat format = new SimpleDateFormat("ss");
                            while (true) {
                                ps.printf("%s %s\n", format.format(new Date()), 10);
                                Thread.sleep(250);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(runnable).start();
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void testUpdateState() throws InterruptedException {
        SparkConf sc = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming Test");
        JavaStreamingContext spark = new JavaStreamingContext(sc, Durations.seconds(1));
        JavaPairDStream<String, Integer> stream = spark.socketTextStream("localhost", 18080)
                .mapToPair(s -> {
                    String[] array = s.split(" ");
                    return new Tuple2<>(array[0], Integer.parseInt(array[1]));
                });
        stream.window(Durations.seconds(5), Durations.seconds(5)).updateStateByKey((Function2<List<Integer>, Optional<Integer>, Optional<Integer>>) (x, y) -> {
            int sum = 0;
            for (Integer integer : x) {
                sum += integer;
            }
            sum += y.orElse(0);
            return Optional.of(sum);
        }).print();
        spark.checkpoint("/tmp/spark-checkpoint");
        spark.start();
        spark.awaitTermination();
    }

    //    @Test
    public void testWindow() throws InterruptedException {
        SparkConf sc = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming Test");
        JavaStreamingContext spark = new JavaStreamingContext(sc, Durations.seconds(1));
        JavaPairDStream<String, Integer> stream = spark.socketTextStream("localhost", 18080)
                .mapToPair(s -> {
                    String[] array = s.split(" ");
                    return new Tuple2<>(array[0], Integer.parseInt(array[1]));
                });


        stream = stream.reduceByKeyAndWindow(Integer::sum, (x, y) -> x - y, Durations.seconds(5), Durations.seconds(2));

        stream.foreachRDD(rdd -> {
            rdd.sortByKey().foreach(tuple -> System.out.printf("%s -> %s\n", tuple._1, tuple._2));
            System.out.println();
        });
        spark.checkpoint("/tmp/spark-checkpoint");
        spark.start();
        spark.awaitTermination();
    }

    //    @Test
    public void test() throws InterruptedException {
        SparkConf sc = new SparkConf().setMaster("local[2]").setAppName("Spark Streaming Test");
        JavaStreamingContext spark = new JavaStreamingContext(sc, Durations.seconds(5));
        JavaPairDStream<String, Integer> stream = spark.socketTextStream("localhost", 18080)
                .mapToPair(s -> {
                    String[] array = s.split(" ");
                    return new Tuple2<>(array[0], Integer.parseInt(array[1]));
                });

        stream.groupByKey().mapToPair(t -> {
            int count = 0;
            for (Integer ignored : t._2) {
                count++;
            }
            return new Tuple2<>(t._1, count);
        }).join(stream.reduceByKey(Integer::sum, 1))
                .foreachRDD((rdd, time) -> {
                    rdd.foreach(tuple -> {
                        System.out.printf("%s -> %s %s\n", tuple._1, tuple._2._1, tuple._2._2);
                    });
                    System.out.println();
                });
        spark.start();
        spark.awaitTermination();
    }
}
