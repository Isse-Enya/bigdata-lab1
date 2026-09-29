import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 实验一 MapReduce 编程（二）：多个文件中整数的升序排序。
 *
 * <p>Map 阶段以整数本身作为 key，Reduce 阶段按照 key 升序到达的顺序编号，
 * 输出"位次 数值"。为保证全局有序，Reduce 任务数设为 1。
 */
public class SortNumbers {

  public static class SortMapper extends Mapper<Object, Text, IntWritable, NullWritable> {
    private final IntWritable number = new IntWritable();

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String text = value.toString().trim();
      if (!text.isEmpty()) {
        number.set(Integer.parseInt(text));
        context.write(number, NullWritable.get());
      }
    }
  }

  public static class SortReducer extends Reducer<IntWritable, NullWritable, NullWritable, Text> {
    private int rank = 0;

    @Override
    protected void reduce(IntWritable key, Iterable<NullWritable> values, Context context)
        throws IOException, InterruptedException {
      rank++;
      context.write(NullWritable.get(), new Text(rank + " " + key.get()));
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("用法: SortNumbers <输入目录> <输出目录>");
      System.exit(2);
    }
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "sort numbers");
    job.setJarByClass(SortNumbers.class);
    job.setMapperClass(SortMapper.class);
    job.setReducerClass(SortReducer.class);
    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(NullWritable.class);
    job.setOutputKeyClass(NullWritable.class);
    job.setOutputValueClass(Text.class);
    job.setNumReduceTasks(1);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
