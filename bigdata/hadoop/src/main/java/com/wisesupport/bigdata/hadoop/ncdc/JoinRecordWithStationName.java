package com.wisesupport.bigdata.hadoop.ncdc;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Iterator;

public class JoinRecordWithStationName extends Configured implements Tool {
    static class JoinStationMapper extends Mapper<LongWritable, Text, TextPair, Text> {
        private final NcdcStationMetadataParser parser = new NcdcStationMetadataParser();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if (parser.parse(value)) {
                context.write(new TextPair(parser.getStationId(), "0"), new Text(parser.getStationName()));
            }
        }
    }

    static class JoinTemperatureMapper extends Mapper<LongWritable, Text, TextPair, Text> {
        private final NcdcRecordParser parser = new NcdcRecordParser();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            parser.parse(value);
            if (parser.isValidTemperature()) {
                context.write(new TextPair(parser.getStationId(), "1"), new Text(parser.getYear() + '\t' + parser.getAirTemperature()));
            }
        }
    }

    static class JoinReducer extends Reducer<TextPair, Text, Text, Text> {
        @Override
        protected void reduce(TextPair key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            String stationName = itr.next().toString();
            while (itr.hasNext()) {
                context.write(key.getFirst(), new Text(stationName + '\t' + itr.next().toString()));
            }
        }
    }

    static class JoinStationPartitioner extends Partitioner<TextPair, Text> {
        @Override
        public int getPartition(TextPair key, Text text, int numPartitions) {
            return (key.getFirst().hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }

    static class JoinStationGroupingComparator extends WritableComparator {
        public JoinStationGroupingComparator() {
            super(TextPair.class, true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            return (((TextPair) a).getFirst()).compareTo(((TextPair) b).getFirst());
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        FileSystem.get(getConf()).delete(new Path("output-join"), true);

        Job job = Job.getInstance(getConf(), "Join Station ");

        MultipleInputs.addInputPath(job, new Path("input/station"), TextInputFormat.class, JoinStationMapper.class);
        MultipleInputs.addInputPath(job, new Path("input/ncdc/all"), TextInputFormat.class, JoinTemperatureMapper.class);
        FileOutputFormat.setOutputPath(job, new Path("output-join"));

        job.setPartitionerClass(JoinStationPartitioner.class);
        job.setGroupingComparatorClass(JoinStationGroupingComparator.class);

        job.setReducerClass(JoinReducer.class);
        job.setOutputKeyClass(TextPair.class);
        job.setOutputValueClass(Text.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new JoinRecordWithStationName(), args));
    }
}
