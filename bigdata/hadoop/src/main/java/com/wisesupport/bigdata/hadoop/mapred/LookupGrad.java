package com.wisesupport.bigdata.hadoop.mapred;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class LookupGrad extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        MapFile.Reader[] readers = MapFileOutputFormat.getReaders(new Path("bigdata/hadoop/samples/grad-map"), getConf());
        Partitioner<IntWritable, Text> partitioner = new HashPartitioner<>();
        Text value = new Text();
        IntWritable key = new IntWritable(66);
        Writable entry = MapFileOutputFormat.getEntry(readers, partitioner, key, value);
        if (entry == null) {
            System.out.printf("key %s not found.\n", key);
        } else {
            System.out.printf("found %s = %s\n", key, value);
        }

        for (MapFile.Reader reader : readers) {
            reader.close();
        }

        readers = MapFileOutputFormat.getReaders(new Path("out"), getConf());
        MapFile.Reader reader = readers[partitioner.getPartition(key, value, readers.length)];
        entry = reader.get(key, value);
        if (entry == null) {
            System.out.printf("key %s not found.\n", key);
        } else {
            System.out.printf("found all : %s = %s\n", key, value);
            IntWritable k = new IntWritable();
            Text v = new Text();
            while (reader.next(k, v)) {
                if (k.equals(key)) {
                    System.out.printf("found all : %s = %s\n", k, v);
                } else {
                    break;
                }
            }
        }
        for (MapFile.Reader r : readers) {
            r.close();
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        ToolRunner.run(new LookupGrad(), args);
    }
}
