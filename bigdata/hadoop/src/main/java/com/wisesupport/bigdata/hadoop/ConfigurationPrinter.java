package com.wisesupport.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.Map;

/**
 * set HADOOP_CLASSPATH=D:\IdeaProjects\WiseSupport\bigdata\hadoop\target\classes
 * hadoop.cmd com.wisesupport.bigdata.hadoop.ConfigurationPrinter
 */
public class ConfigurationPrinter extends Configured implements Tool {
    @Override
    public int run(String[] args) {
        Configuration configuration = getConf();
        for (Map.Entry<String, String> entry : configuration) {
            System.out.printf("%s => %s\n", entry.getKey(), entry.getValue());
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new ConfigurationPrinter(), args);
    }
}
