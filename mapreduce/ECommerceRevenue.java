import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ECommerceRevenue {

    public static class RevenueMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {

        private Text country = new Text();
        private DoubleWritable revenue = new DoubleWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // Skip header
            if (line.startsWith("InvoiceNo")) {
                return;
            }

            String[] fields = line.split(",", -1);

            // CSV must have 9 columns
            if (fields.length < 9) {
                return;
            }

            try {
                String countryName = fields[7].trim();
                double totalPrice = Double.parseDouble(fields[8].trim());

                country.set(countryName);
                revenue.set(totalPrice);

                context.write(country, revenue);

            } catch (NumberFormatException e) {
                // Ignore invalid rows
            }
        }
    }

    public static class RevenueReducer
            extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        private DoubleWritable result = new DoubleWritable();

        public void reduce(Text key, Iterable<DoubleWritable> values,
                           Context context)
                throws IOException, InterruptedException {

            double sum = 0.0;

            for (DoubleWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: ECommerceRevenue <input> <output>");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "E-Commerce Revenue by Country");

        job.setJarByClass(ECommerceRevenue.class);

        job.setMapperClass(RevenueMapper.class);
        job.setReducerClass(RevenueReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}