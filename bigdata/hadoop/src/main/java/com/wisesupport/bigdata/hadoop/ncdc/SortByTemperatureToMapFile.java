package com.wisesupport.bigdata.hadoop.ncdc;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;

public class SortByTemperatureToMapFile extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), "SortByTemperatureToMapFile");

        job.setInputFormatClass(SequenceFileInputFormat.class);
        SequenceFileInputFormat.addInputPath(job, new Path("input/ncdc/all-seq/part-m-00001"));
        SequenceFileInputFormat.addInputPath(job, new Path("input/ncdc/all-seq/part-m-00000"));

        job.setNumReduceTasks(30);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputPath(job, new Path("output-map"));
        SequenceFileOutputFormat.setCompressOutput(job, true);
        SequenceFileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.BLOCK);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File("output-map"));
        System.exit(ToolRunner.run(new SortByTemperatureToMapFile(), args));
    }
}
