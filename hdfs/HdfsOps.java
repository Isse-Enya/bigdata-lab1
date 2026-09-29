import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;

/**
 * 实验一 HDFS 常用操作：使用 Hadoop Java API 实现十项文件操作。
 *
 * <p>用法：java -cp "build:$(hadoop classpath)" HdfsOps &lt;子命令&gt; [参数...]
 */
public class HdfsOps {

  private static FileSystem fs;
  private static final SimpleDateFormat TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      usage();
      System.exit(1);
    }
    Configuration conf = new Configuration();
    fs = FileSystem.get(URI.create("hdfs://localhost:9000"), conf);
    String cmd = args[0];
    try {
      switch (cmd) {
        case "upload":
          upload(args);
          break;
        case "download":
          download(args);
          break;
        case "cat":
          cat(args);
          break;
        case "info":
          info(args);
          break;
        case "lsr":
          lsr(args);
          break;
        case "mkfile":
          mkfile(args);
          break;
        case "rmfile":
          rmfile(args);
          break;
        case "mkdir":
          mkdir(args);
          break;
        case "rmdir":
          rmdir(args);
          break;
        case "append":
          append(args);
          break;
        case "rm":
          rm(args);
          break;
        case "move":
          move(args);
          break;
        default:
          usage();
      }
    } finally {
      fs.close();
    }
  }

  /** 功能 1：上传文件，若目标已存在则由用户选择追加还是覆盖。 */
  private static void upload(String[] args) throws Exception {
    if (args.length < 3) {
      System.out.println("用法: upload <本地文件> <HDFS路径> [append|overwrite]");
      return;
    }
    String local = args[1];
    Path remote = new Path(args[2]);
    if (fs.exists(remote)) {
      String mode = args.length > 3 ? args[3] : ask("文件 " + remote + " 已存在，请选择 append(追加) 或 overwrite(覆盖): ");
      if ("append".equalsIgnoreCase(mode)) {
        try (InputStream in = new FileInputStream(local);
            FSDataOutputStream out = fs.append(remote)) {
          IOUtils.copyBytes(in, out, 4096, false);
        }
        System.out.println("已把本地文件追加到 " + remote + " 末尾");
      } else if ("overwrite".equalsIgnoreCase(mode)) {
        fs.copyFromLocalFile(false, true, new Path(local), remote);
        System.out.println("已覆盖 " + remote);
      } else {
        System.out.println("未识别的选项: " + mode + "，操作取消");
      }
    } else {
      fs.copyFromLocalFile(false, true, new Path(local), remote);
      System.out.println("已上传 " + local + " -> " + remote);
    }
  }

  /** 功能 2：下载文件，若本地同名文件已存在则自动重命名。 */
  private static void download(String[] args) throws Exception {
    if (args.length < 3) {
      System.out.println("用法: download <HDFS文件> <本地目录>");
      return;
    }
    Path remote = new Path(args[1]);
    File dir = new File(args[2]);
    if (!dir.exists() && !dir.mkdirs()) {
      System.out.println("无法创建本地目录 " + dir);
      return;
    }
    String name = remote.getName();
    File local = new File(dir, name);
    int index = 1;
    while (local.exists()) {
      String base = name;
      String suffix = "";
      int dot = name.lastIndexOf('.');
      if (dot > 0) {
        base = name.substring(0, dot);
        suffix = name.substring(dot);
      }
      local = new File(dir, base + "_" + index + suffix);
      index++;
    }
    fs.copyToLocalFile(false, remote, new Path(local.getAbsolutePath()), true);
    System.out.println("已下载 " + remote + " -> " + local.getAbsolutePath());
  }

  /** 功能 3：把 HDFS 文件内容输出到终端。 */
  private static void cat(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: cat <HDFS文件>");
      return;
    }
    try (FSDataInputStream in = fs.open(new Path(args[1]))) {
      IOUtils.copyBytes(in, System.out, 4096, false);
    }
  }

  /** 功能 4：显示文件的读写权限、大小、创建时间、路径等信息。 */
  private static void info(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: info <HDFS路径>");
      return;
    }
    printStatus(fs.getFileStatus(new Path(args[1])));
  }

  /** 功能 5：递归显示目录下所有文件的详细信息。 */
  private static void lsr(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: lsr <HDFS目录>");
      return;
    }
    RemoteIterator<LocatedFileStatus> iterator = fs.listFiles(new Path(args[1]), true);
    int count = 0;
    while (iterator.hasNext()) {
      printStatus(iterator.next());
      count++;
    }
    System.out.println("共 " + count + " 个文件");
  }

  /** 功能 6：创建文件，父目录不存在时自动创建。 */
  private static void mkfile(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: mkfile <HDFS文件>");
      return;
    }
    Path path = new Path(args[1]);
    Path parent = path.getParent();
    if (parent != null && !fs.exists(parent)) {
      fs.mkdirs(parent);
      System.out.println("父目录不存在，已自动创建 " + parent);
    }
    if (fs.exists(path)) {
      System.out.println(path + " 已存在，未重复创建");
      return;
    }
    fs.create(path).close();
    System.out.println("已创建文件 " + path);
  }

  /** 功能 6 的删除部分：删除 HDFS 文件。 */
  private static void rmfile(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: rmfile <HDFS文件>");
      return;
    }
    Path path = new Path(args[1]);
    if (fs.delete(path, false)) {
      System.out.println("已删除文件 " + path);
    } else {
      System.out.println("删除失败，" + path + " 不存在");
    }
  }

  /** 功能 7 的创建部分：创建目录，父目录不存在时自动创建。 */
  private static void mkdir(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: mkdir <HDFS目录>");
      return;
    }
    Path path = new Path(args[1]);
    if (fs.mkdirs(path)) {
      System.out.println("已创建目录 " + path);
    } else {
      System.out.println(path + " 已存在");
    }
  }

  /** 功能 7 的删除部分：目录为空时删除，不为空时拒绝删除。 */
  private static void rmdir(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("用法: rmdir <HDFS目录>");
      return;
    }
    Path path = new Path(args[1]);
    if (!fs.exists(path)) {
      System.out.println(path + " 不存在");
      return;
    }
    if (!fs.getFileStatus(path).isDirectory()) {
      System.out.println(path + " 不是目录");
      return;
    }
    if (fs.listStatus(path).length > 0) {
      System.out.println("目录 " + path + " 非空，拒绝删除");
      return;
    }
    fs.delete(path, false);
    System.out.println("已删除空目录 " + path);
  }

  /** 功能 8：向文件追加内容，位置由用户指定（开头或结尾）。 */
  private static void append(String[] args) throws Exception {
    if (args.length < 4) {
      System.out.println("用法: append <HDFS文件> <内容> <head|tail>");
      return;
    }
    Path path = new Path(args[1]);
    String content = args[2];
    String position = args[3];
    if (!fs.exists(path)) {
      System.out.println(path + " 不存在");
      return;
    }
    if ("tail".equalsIgnoreCase(position)) {
      try (FSDataOutputStream out = fs.append(path)) {
        out.write((content + "\n").getBytes("UTF-8"));
      }
      System.out.println("已在 " + path + " 末尾追加内容");
    } else if ("head".equalsIgnoreCase(position)) {
      // HDFS 不支持在文件头部插入，做法是读取原内容后重写：新内容 + 原内容
      StringBuilder old = new StringBuilder();
      try (FSDataInputStream in = fs.open(path)) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
          old.append(line).append('\n');
        }
      }
      Path tmp = new Path(path.toString() + ".tmp");
      try (FSDataOutputStream out = fs.create(tmp, true)) {
        out.write((content + "\n").getBytes("UTF-8"));
        out.write(old.toString().getBytes("UTF-8"));
      }
      fs.delete(path, false);
      fs.rename(tmp, path);
      System.out.println("已在 " + path + " 开头写入内容");
    } else {
      System.out.println("位置参数只能是 head 或 tail");
    }
  }

  /** 功能 9：删除 HDFS 中指定的文件。 */
  private static void rm(String[] args) throws Exception {
    rmfile(args);
  }

  /** 功能 10：把文件从源路径移动到目的路径。 */
  private static void move(String[] args) throws Exception {
    if (args.length < 3) {
      System.out.println("用法: move <源路径> <目的路径>");
      return;
    }
    Path src = new Path(args[1]);
    Path dst = new Path(args[2]);
    Path parent = dst.getParent();
    if (parent != null && !fs.exists(parent)) {
      fs.mkdirs(parent);
    }
    if (fs.rename(src, dst)) {
      System.out.println("已把 " + src + " 移动到 " + dst);
    } else {
      System.out.println("移动失败");
    }
  }

  private static void printStatus(FileStatus status) {
    System.out.printf(
        "%s%s  %10d  %s  %s%n",
        status.isDirectory() ? "d" : "-",
        status.getPermission().toString(),
        status.getLen(),
        TIME.format(new Date(status.getModificationTime())),
        status.getPath().toString());
  }

  private static String ask(String prompt) throws Exception {
    System.out.print(prompt);
    System.out.flush();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    String line = reader.readLine();
    return line == null ? "" : line.trim();
  }

  private static void usage() {
    System.out.println("HDFS 常用操作 Java API 实现，用法：");
    System.out.println("  upload   <本地文件> <HDFS路径> [append|overwrite]");
    System.out.println("  download <HDFS文件> <本地目录>");
    System.out.println("  cat      <HDFS文件>");
    System.out.println("  info     <HDFS路径>");
    System.out.println("  lsr      <HDFS目录>");
    System.out.println("  mkfile   <HDFS文件>");
    System.out.println("  rmfile   <HDFS文件>");
    System.out.println("  mkdir    <HDFS目录>");
    System.out.println("  rmdir    <HDFS目录>");
    System.out.println("  append   <HDFS文件> <内容> <head|tail>");
    System.out.println("  rm       <HDFS路径>");
    System.out.println("  move     <源路径> <目的路径>");
  }
}
