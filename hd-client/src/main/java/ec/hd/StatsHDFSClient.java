package ec.hd;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import ec.stats.StatsSummary;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

public class StatsHDFSClient {
    public static void main(String[] args) {
        String hdfsUri = "hdfs://localhost:19000/stats/part-r-00000";
        String localFilePath = "C:/enterprise/tmp/model/hdstats.bin";

        Configuration conf = new Configuration();
        conf.addResource(new Path("file:///C:/enterprise/hadoop-2.7.1/etc/hadoop/core-site.xml"));
        conf.addResource(new Path("file:///C:/enterprise/hadoop-2.7.1/etc/hadoop/hdfs-site.xml"));
        conf.set("fs.defaultFS", "hdfs://localhost:19000");
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        try (FileSystem fs = FileSystem.get(URI.create(hdfsUri), conf);
             InputStream in = fs.open(new Path(hdfsUri))) {

            // Read the data from HDFS (assuming it is in the format: stats <count>,<min>,<max>,<mean>,<std>)
            byte[] buffer = new byte[4096];
            int bytesRead = in.read(buffer);
            String statsData = new String(buffer, 0, bytesRead).trim();

            // Remove the "stats" key from the beginning of the line
            if (statsData.startsWith("stats")) {
                statsData = statsData.substring(5).trim(); // Remove "stats" and the following whitespace
            }

            // Split the remaining data into parts
            String[] statsParts = statsData.split(",");
            if (statsParts.length != 5) {
                throw new RuntimeException("Unexpected stats format in HDFS file.");
            }

            // Create StatsSummary object
            StatsSummary statsSummary = new StatsSummary();
            statsSummary.setCount(Integer.parseInt(statsParts[0].trim()));
            statsSummary.setMin(Double.parseDouble(statsParts[1].trim()));
            statsSummary.setMax(Double.parseDouble(statsParts[2].trim()));
            statsSummary.setMean(Double.parseDouble(statsParts[3].trim()));
            statsSummary.setSTD(Double.parseDouble(statsParts[4].trim()));

            // Serialize StatsSummary to a local file
            try (FileOutputStream fileOut = new FileOutputStream(localFilePath);
                 ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
                objOut.writeObject(statsSummary);
                System.out.println("Stats summary saved to " + localFilePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
