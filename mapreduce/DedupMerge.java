import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 实验一 MapReduce 编程（一）：两个文件的合并与去重。
 *
 * <p>Map 阶段把每行内容作为 key 输出，相同内容在 Shuffle 阶段被分到同一组，
 * Reduce 阶段每组只输出一次，从而同时完成合并与去重。
 */
public class DedupMerge {

  public static class DedupMapper extends Mapper<Object, Text, Text, NullWritable> {
    private final Text line = new Text();

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String text = value.toString().trim();
      if (!text.isEmpty()) {
        line.set(text);
        context.write(line, NullWritable.get());
      }
    }
  }

  public static class DedupReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
    @Override
    protected void reduce(Text key, Iterable<NullWritable> values, Context context)
        throws IOException, InterruptedException {
      // 同一个 key 的所有记录只输出一次，实现去重
      context.write(key, NullWritable.get());
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("用法: DedupMerge <输入目录> <输出目录>");
      System.exit(2);
    }
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "merge and dedup");
    job.setJarByClass(DedupMerge.class);
    job.setMapperClass(DedupMapper.class);
    job.setReducerClass(DedupReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(NullWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
