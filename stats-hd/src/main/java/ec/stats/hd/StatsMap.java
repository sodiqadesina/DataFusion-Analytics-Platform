package ec.stats.hd;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class StatsMap extends Mapper<LongWritable, Text, Text, StatsWritable> {
    private final static Text STATS_KEY = new Text("stats");
    private StatsWritable stats = new StatsWritable();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        try {
            double number = Double.parseDouble(line.trim());
            stats.set(1, number, number, number, number * number);
            context.write(STATS_KEY, stats);
        } catch (NumberFormatException e) {
            // Ignore invalid lines
        }
    }
}