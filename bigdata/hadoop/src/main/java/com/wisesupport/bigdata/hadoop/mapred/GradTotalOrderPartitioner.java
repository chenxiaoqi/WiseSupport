package com.wisesupport.bigdata.hadoop.mapred;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

public class GradTotalOrderPartitioner extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf());

        job.setInputFormatClass(SequenceFileInputFormat.class);
        SequenceFileInputFormat.addInputPath(job, new Path("bigdata/hadoop/samples/grad-seq"));

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputPath(job, new Path("out"));

        job.setPartitionerClass(TotalOrderPartitioner.class);
        InputSampler.Sampler<IntWritable, Text> sampler = new InputSampler.RandomSampler<>(1, 2, 2);
        InputSampler.writePartitionFile(job, sampler);

//        String partitionFile = TotalOrderPartitioner.getPartitionFile(getConf());
//        URI uri = new URI(partitionFile + "#" + TotalOrderPartitioner.DEFAULT_PATH);
//        DistributedCache.addCacheFile(uri, getConf());
//        DistributedCache.createSymlink(getConf());
        int exitCode = job.waitForCompletion(true) ? 0 : 1;


        try (SequenceFile.Reader reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path("out/part-r-00000")))) {
            IntWritable key = new IntWritable();
            Text value = new Text();
            while (reader.next(key, value)) {
                System.out.printf("%s : %s = %s\n", "out/part-r-00000", key.get(), value.toString());
            }
        }

//        try (SequenceFile.Reader reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path("_partition.lst")))) {
//            NullWritable key = NullWritable.get();
//            Text value = new Text();
//            while (reader.next(key, value)) {
//                System.out.printf("%s : %s = %s\n", "_partition", key, value.toString());
//            }
//        }
        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File("out"));
        System.exit(ToolRunner.run(new GradTotalOrderPartitioner(), args));
    }
}
