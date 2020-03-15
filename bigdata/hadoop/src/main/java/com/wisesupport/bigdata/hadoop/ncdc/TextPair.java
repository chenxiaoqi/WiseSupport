package com.wisesupport.bigdata.hadoop.ncdc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class TextPair implements WritableComparable<TextPair> {
    private Text first = new Text();

    private Text last = new Text();

    public TextPair() {
    }

    public TextPair(String f, String l) {
        this.first.set(f);
        this.last.set(l);
    }

    @Override
    public int compareTo(TextPair o) {
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

    public Text getFirst() {
        return first;
    }

    public Text getLast() {
        return last;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextPair) {
            TextPair pair = (TextPair) obj;
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
