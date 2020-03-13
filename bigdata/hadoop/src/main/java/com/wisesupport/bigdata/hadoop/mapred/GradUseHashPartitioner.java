package com.wisesupport.bigdata.hadoop.mapred;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.util.Arrays;

public class GradUseHashPartitioner extends Configured implements Tool {
    @Override
    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf());

        job.setInputFormatClass(SequenceFileInputFormat.class);
        SequenceFileInputFormat.addInputPath(job, new Path("bigdata/hadoop/samples/grad-seq"));

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        // 两个reduce任务,生成的两个结果文件直接合并不是排好序的
        job.setNumReduceTasks(2);

        SequenceFileOutputFormat.setOutputPath(job, new Path("out"));
        SequenceFileOutputFormat.setCompressOutput(job, true);
        SequenceFileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
        SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.BLOCK);
        int exitCode = job.waitForCompletion(true) ? 0 : 1;

        for (String path : Arrays.asList("out/part-r-00000", "out/part-r-00001")) {
            try (SequenceFile.Reader reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path(path)))) {
                IntWritable key = new IntWritable();
                Text value = new Text();
                while (reader.next(key, value)) {
                    System.out.printf("%s : %s = %s\n", path, key.get(), value.toString());
                }
            }
        }


        return exitCode;
    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteDirectory(new File("out"));
        System.exit(ToolRunner.run(new GradUseHashPartitioner(), args));
    }
}
