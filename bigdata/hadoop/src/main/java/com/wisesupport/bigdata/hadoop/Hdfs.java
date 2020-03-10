package com.wisesupport.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import java.io.IOException;

public class Hdfs {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        try (FileSystem fs = FileSystem.newInstance(conf)) {
            RemoteIterator<LocatedFileStatus> itr = fs.listFiles(new Path("/user/chenxiaoqi/input"), false);
            while (itr.hasNext()) {
                LocatedFileStatus status = itr.next();
                System.out.println(status.getPath().getName());
            }
        }
    }
}
