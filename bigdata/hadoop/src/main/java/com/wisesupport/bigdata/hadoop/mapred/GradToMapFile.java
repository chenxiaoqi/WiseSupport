package com.wisesupport.bigdata.hadoop.mapred;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;

public class GradToMapFile extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf());

        job.setInputFormatClass(SequenceFileInputFormat.class);
        SequenceFileInputFormat.addInputPath(job, new Path("bigdata/hadoop/samples/grad-hash"));

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputFormatClass(MapFileOutputFormat.class);

        // 两个reduce任务,生成的两个结果文件直接合并不是排好序的
        job.setNumReduceTasks(2);

        MapFileOutputFormat.setOutputPath(job, new Path("out"));
        MapFileOutputFormat.setCompressOutput(job, true);
        MapFileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        int exitCode = job.waitForCompletion(true) ? 0 : 1;

        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File("out"));
        System.exit(ToolRunner.run(new GradToMapFile(), args));
    }
}
