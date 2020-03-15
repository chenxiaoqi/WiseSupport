package com.wisesupport.bigdata.hadoop.ncdc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class IntPair implements WritableComparable<IntPair> {
    private IntWritable first = new IntWritable();

    private IntWritable last = new IntWritable();

    public IntPair() {
    }

    public IntPair(int f, int l) {
        this.first.set(f);
        this.last.set(l);
    }

    @Override
    public int compareTo(IntPair o) {
        int result = this.first.compareTo(o.first);
        if (result == 0) {
            result = this.last.compareTo(o.last);
        }
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        first.write(out);
        last.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.first.readFields(in);
        this.last.readFields(in);
    }

    public IntWritable getFirst() {
        return first;
    }

    public IntWritable getLast() {
        return last;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntPair) {
            IntPair pair = (IntPair) obj;
            return Objects.equals(this.first, pair.first) && Objects.equals(this.last, pair.last);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.first.toString() + '\t' + this.last.toString();
    }
}
