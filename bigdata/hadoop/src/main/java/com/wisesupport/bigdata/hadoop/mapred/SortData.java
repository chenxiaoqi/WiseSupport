package com.wisesupport.bigdata.hadoop.mapred;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class SortData extends Configured implements Tool {
    public static class GradMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(value.toString(), " \t");
            String name;
            int grad;
            if (tokenizer.hasMoreTokens()) {
                name = tokenizer.nextToken();
            } else {
                return;
            }
            if (tokenizer.hasMoreTokens()) {
                grad = Integer.parseInt(tokenizer.nextToken());
            } else {
                return;
            }
            context.write(new IntWritable(grad), new Text(name));
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), "Sort Data");
        job.setMapperClass(GradMapper.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        // 设置reduce task 为0,结果就不排序了 生成的文件名是 part-m-00000
        job.setNumReduceTasks(0);
        FileInputFormat.addInputPath(job, new Path("bigdata/hadoop/samples/grad.txt"));

//        FileOutputFormat.setOutputPath(job, new Path("out"));

        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        SequenceFileOutputFormat.setOutputPath(job, new Path("out"));
        SequenceFileOutputFormat.setCompressOutput(job, false);
        SequenceFileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.BLOCK);
        int exitCode = job.waitForCompletion(true) ? 0 : 1;

        try (SequenceFile.Reader reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path("out/part-m-00000")))) {
            IntWritable key = new IntWritable();
            Text value = new Text();
            while (reader.next(key, value)) {
                System.out.printf("seq : %s = %s\n", key.get(), value.toString());
            }
        }

        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File("out"));
        System.exit(ToolRunner.run(new SortData(), args));
    }
}
