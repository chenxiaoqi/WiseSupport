package com.wisesupport.bigdata.hadoop;

import com.wisesupport.bigdata.hadoop.ncdc.NcdcRecordParser;
import com.wisesupport.bigdata.hadoop.ncdc.NcdcStationMetadata;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;

public class HadoopUtils extends Configured implements Tool {

    public void printSequenceFile(String path) throws IOException {
        NcdcRecordParser parser = new NcdcRecordParser();
        try (SequenceFile.Reader reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path(path)))) {
            IntWritable key = new IntWritable();
            Text value = new Text();
            while (reader.next(key, value)) {
                parser.parse(value);
                System.out.printf("%s : %s = %s\n", path, key.get(), parser.getYear());
            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
//        printSequenceFile("input/ncdc/all-seq/part-m-00000");
//        printSequenceFile("input/ncdc/all-seq/part-m-00001");
//        printSequenceFile("output-hashsort/part-r-00000");
//        printSequenceFile("output-hashsort/part-r-00018");
//        printSequenceFile("output-totalsort/part-r-00000");
//        printSequenceFile("output-totalsort/part-r-00029");
        return 0;
    }

    public static void main(String[] args) throws Exception {
        NcdcStationMetadata metadata = new NcdcStationMetadata();
        metadata.initialize(new File("input/station/stations-fixed-width.txt"));
        System.out.println(metadata.getStationIdToNameMap());
        System.exit(ToolRunner.run(new HadoopUtils(), args));
    }
}
