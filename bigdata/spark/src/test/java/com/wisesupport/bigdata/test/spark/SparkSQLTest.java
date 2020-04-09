package com.wisesupport.bigdata.test.spark;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

public class SparkSQLTest {

    @Test
    public void test() {
        SparkSession spark = SparkSession.builder().appName("Test SparkSQL").master("local").getOrCreate();
        Dataset<Row> person = spark.read().json("samples/Person.json");
        person.printSchema();
        person.show();
        person.select(new Column("id").plus(1).alias("nid")).write().parquet("person.parquet");
        spark.close();
    }
}
