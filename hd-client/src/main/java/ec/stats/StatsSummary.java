package ec.stats;

import java.io.Serializable;

public class StatsSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private int count = 0;
    private double min = 0;
    private double max = 0;
    private double mean = 0;
    private double std = 0;

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }

    public double getMean() { return mean; }
    public void setMean(double mean) { this.mean = mean; }

    public double getSTD() { return std; }
    public void setSTD(double std) { this.std = std; }

    @Override
    public String toString() {
        return "StatsSummary{" +
                "count=" + count +
                ", min=" + min +
                ", max=" + max +
                ", mean=" + mean +
                ", std=" + std +
                '}';
    }
}