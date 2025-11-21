package ec.stats.hd;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class StatsMR {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: StatsMR <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Stats Computing");
        job.setJarByClass(StatsMR.class);

        job.setMapperClass(StatsMap.class);
        job.setCombinerClass(StatsCombine.class);
        job.setReducerClass(StatsCombine.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(StatsWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}