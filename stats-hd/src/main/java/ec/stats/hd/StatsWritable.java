package ec.stats.hd;

import org.apache.hadoop.io.Writable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StatsWritable implements Writable {
    private long count;
    private double min;
    private double max;
    private double sum;
    private double sumOfSquares;

    public StatsWritable() {
        this.count = 0;
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
        this.sum = 0.0;
        this.sumOfSquares = 0.0;
    }

    // Getters and setters
    public long getCount() { return count; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getSum() { return sum; }
    public double getSumOfSquares() { return sumOfSquares; }

    public void set(long count, double min, double max, double sum, double sumOfSquares) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.sumOfSquares = sumOfSquares;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(count);
        out.writeDouble(min);
        out.writeDouble(max);
        out.writeDouble(sum);
        out.writeDouble(sumOfSquares);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        count = in.readLong();
        min = in.readDouble();
        max = in.readDouble();
        sum = in.readDouble();
        sumOfSquares = in.readDouble();
    }

    @Override
    public String toString() {
        double mean = count > 0 ? sum / count : 0.0;
        double variance = count > 0 ? (sumOfSquares / count) - (mean * mean) : 0.0;
        double std = Math.sqrt(variance);
        return count + "," + min + "," + max + "," + mean + "," + std;
    }
}