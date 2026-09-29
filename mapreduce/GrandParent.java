import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 实验一 MapReduce 编程（三）：从 child-parent 表挖掘祖孙关系。
 *
 * <p>Map 阶段对输入行 "child parent" 输出两条记录：&lt;child, P:parent&gt; 与
 * &lt;parent, C:child&gt;。Reduce 阶段以中间辈为 key 做一次表的自身连接：
 * 把所有"我的孩子"与"我的父母"两两组合，即得到"孙辈 祖辈"关系。
 */
public class GrandParent {

  public static class RelationMapper extends Mapper<Object, Text, Text, Text> {
    private final Text outKey = new Text();
    private final Text outValue = new Text();

    @Override
    protected void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
      String[] tokens = value.toString().trim().split("\\s+");
      if (tokens.length != 2) {
        return;
      }
      String child = tokens[0];
      String parent = tokens[1];
      // 以 child 为 key：记录 child 的父母
      outKey.set(child);
      outValue.set("P:" + parent);
      context.write(outKey, outValue);
      // 以 parent 为 key：记录 parent 的孩子
      outKey.set(parent);
      outValue.set("C:" + child);
      context.write(outKey, outValue);
    }
  }

  public static class RelationReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      Set<String> parents = new HashSet<>();   // key 的父母
      Set<String> children = new HashSet<>();  // key 的孩子
      for (Text value : values) {
        String text = value.toString();
        if (text.startsWith("P:")) {
          parents.add(text.substring(2));
        } else if (text.startsWith("C:")) {
          children.add(text.substring(2));
        }
      }
      // key 的孩子与 key 的父母构成祖孙关系
      for (String child : children) {
        for (String parent : parents) {
          context.write(new Text(child), new Text(parent));
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("用法: GrandParent <输入目录> <输出目录>");
      System.exit(2);
    }
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "grand parent mining");
    job.setJarByClass(GrandParent.class);
    job.setMapperClass(RelationMapper.class);
    job.setReducerClass(RelationReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
