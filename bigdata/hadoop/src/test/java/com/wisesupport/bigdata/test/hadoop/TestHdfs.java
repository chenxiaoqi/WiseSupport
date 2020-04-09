package com.wisesupport.bigdata.test.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.io.IOException;

public class TestHdfs {
    private static FileSystem FS = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        FS = FileSystem.newInstance(new Configuration());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        FS.close();
    }

    @Test
    public void testGlob() throws IOException {
        FileStatus[] fileStatuses = FS.globStatus(new Path("/user/chenxiaoqi/{input,output}/*"));
        for (FileStatus fileStatus : fileStatuses) {
            System.out.println(fileStatus.getPath().toString());
        }
    }

    @Test
    public void test() throws IOException {
        RemoteIterator<LocatedFileStatus> itr = FS.listFiles(new Path("/"), true);
        while (itr.hasNext()) {
            LocatedFileStatus status = itr.next();
            System.out.println(status.getPath().toString());
        }
    }

    @Test
    public void testReadAndWrite() throws IOException {
        Path path = new Path("/user/chenxiaoqi/readme.txt");
        try (FSDataOutputStream out = FS.create(path, true, 512, () -> {
            System.out.println("progress");
        })) {

            out.writeUTF("hello world!");
        }
        try (FSDataInputStream in = FS.open(path)) {
            IOUtils.copyBytes(in, System.out, 512);
        }

        Assert.assertTrue(FS.delete(path, false));
    }
}
