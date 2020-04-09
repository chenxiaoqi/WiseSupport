package com.wisesupport.bigdata.test.hadoop;

import com.wisesupport.bigdata.hadoop.mapred.WordCount;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class TestWordCount {

    private static FileSystem FS = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        FS = FileSystem.newInstance(new Configuration());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        FS.close();
    }

    @Before
    public void before() throws IOException {
        if (!FS.delete(new Path("output"), true)) {
            System.out.println("delete output failed.");
        }
    }

    @Test
    public void test() throws Exception {
        new WordCount() {
            @Override
            protected void config(Job job) {
                job.setJar("target/hadoop-1.0.0.jar");
            }
        }.run(new String[]{"input/*", "output"});
    }
}
