package com.wisesupport.bigdata.hadoop.ncdc;

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

public class LookupRecordByTemperature extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        MapFile.Reader[] readers = MapFileOutputFormat.getReaders(new Path("output-map"), getConf());
        Partitioner<IntWritable, Text> partitioner = new HashPartitioner<>();
        Text value = new Text();
        IntWritable key = new IntWritable(-133);
        Writable entry = MapFileOutputFormat.getEntry(readers, partitioner, key, value);
        if (entry == null) {
            System.out.printf("key %s not found.\n", key);
        } else {
            System.out.printf("found %s = %s\n", key, value);
        }

        for (MapFile.Reader reader : readers) {
            reader.close();
        }

        readers = MapFileOutputFormat.getReaders(new Path("output-map"), getConf());
        int partition = partitioner.getPartition(key, value, readers.length);
        MapFile.Reader reader = readers[partition];
        entry = reader.get(key, value);
        if (entry == null) {
            System.out.printf("key %s not found.\n", key);
        } else {
            System.out.printf("found all - %s: %s = %s\n", partition, key, value);
            IntWritable k = new IntWritable();
            Text v = new Text();
            while (reader.next(k, v)) {
                if (k.equals(key)) {
                    System.out.printf("found all - %s: %s = %s\n", partition, k, v);
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
        ToolRunner.run(new LookupRecordByTemperature(), args);
    }
}
