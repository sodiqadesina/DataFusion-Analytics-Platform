package ec.stats.hd;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

//Reducer Class
public class StatsReduce extends Reducer<Text, StatsWritable, Text, StatsWritable> {
 private StatsWritable result = new StatsWritable();

 @Override
 protected void reduce(Text key, Iterable<StatsWritable> values, Context context) throws IOException, InterruptedException {
     long count = 0;
     double min = Double.MAX_VALUE;
     double max = Double.MIN_VALUE;
     double sum = 0.0;
     double sumOfSquares = 0.0;

     for (StatsWritable val : values) {
         count += val.getCount();
         min = Math.min(min, val.getMin());
         max = Math.max(max, val.getMax());
         sum += val.getSum();
         sumOfSquares += val.getSumOfSquares();
     }

     result.set(count, min, max, sum, sumOfSquares);
     context.write(key, result);
 }
}
