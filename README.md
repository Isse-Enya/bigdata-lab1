# 大数据管理与分析 实验一 代码

本仓库为《大数据管理与分析》课程实验一"大数据系统基本实验"的源代码，包含三部分内容：
Linux 常用命令、HDFS 常用操作（Shell 命令与 Java API）以及 MapReduce 初级编程。

## 目录结构

```
bigdata-lab1/
├── linux/
│   └── linux_commands.md        实验一 Linux 命令操作记录
├── hdfs/
│   ├── HdfsOps.java             HDFS Java API 实现的十项文件操作
│   └── hdfs_shell.md            与 Java API 等价的 HDFS Shell 命令
├── mapreduce/
│   ├── DedupMerge.java          文件的合并与去重
│   ├── SortNumbers.java         多文件整数升序排序并输出位次
│   ├── GrandParent.java         从 child-parent 表挖掘祖孙关系
│   └── input/                   三个程序的样例输入数据
├── run_all.sh                   一键编译并运行全部程序
└── README.md
```

## 运行环境

* 操作系统：Ubuntu 24.04.2 LTS 64 位
* JDK：OpenJDK 11（`/usr/lib/jvm/java-11-openjdk-amd64`）
* Hadoop：3.3.6，伪分布式部署（`HADOOP_HOME=~/hadoop`，`fs.defaultFS=hdfs://localhost:9000`）

## 编译与运行

```bash
# 环境变量
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export HADOOP_HOME=$HOME/hadoop
export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop

# 编译（HDFS Java API 与 MapReduce 程序共用一条 classpath）
mkdir -p build
javac -encoding UTF-8 -cp "$(hadoop classpath)" -d build \
      hdfs/HdfsOps.java mapreduce/*.java

# HDFS Java API 示例
java -cp "build:$(hadoop classpath)" HdfsOps lsr /user/hadoop

# MapReduce 作业：必须先打包成 jar 再提交。
# 直接用 java -cp 提交本地类时，YARN 上的任务容器会报 ClassNotFoundException。
jar cf lab1.jar -C build .
hadoop jar lab1.jar DedupMerge /lab1/input/dedup /lab1/output/dedup

# 或者直接执行一键脚本，完成编译、上传数据并依次运行三个作业
bash run_all.sh
```

## 程序说明

### HdfsOps.java

以子命令方式实现实验要求的十项 HDFS 操作，全部基于 HDFS Java API（`Configuration`、
`FileSystem`、`Path`、`FSDataInputStream`、`FSDataOutputStream`、`FileStatus`）：

| 子命令 | 功能 |
| --- | --- |
| `upload <本地文件> <HDFS路径> [append\|overwrite]` | 上传文件，目标已存在时由用户选择追加或覆盖 |
| `download <HDFS文件> <本地目录>` | 下载文件，本地重名时自动重命名 |
| `cat <HDFS文件>` | 输出文件内容到终端 |
| `info <HDFS路径>` | 显示权限、大小、修改时间、路径 |
| `lsr <HDFS目录>` | 递归显示目录下所有文件的上述信息 |
| `mkfile <HDFS文件>` | 创建文件，父目录不存在时自动创建 |
| `rmfile <HDFS文件>` | 删除文件 |
| `mkdir <HDFS目录>` | 创建目录，父目录不存在时自动创建 |
| `rmdir <HDFS目录>` | 仅当目录为空时删除 |
| `append <HDFS文件> <内容> <head\|tail>` | 在文件开头或结尾追加内容 |
| `rm <HDFS路径>` | 删除文件 |
| `move <源路径> <目的路径>` | 移动/重命名 |

### MapReduce 程序

* `DedupMerge`：Map 阶段将每一行作为 key 输出，Reduce 阶段对同一 key 只输出一次，
  借助 Shuffle 的分组能力完成两个文件的合并与去重。
* `SortNumbers`：Map 阶段以整数本身作为 key，Reduce 阶段按 key 升序到达的顺序编号，
  使用单个 Reduce 任务保证全局有序，输出"位次 数值"。
* `GrandParent`：Map 阶段对每一行 `child parent` 输出 `<child, P:parent>` 与
  `<parent, C:child>`，Reduce 阶段以中间辈为 key 做表的自身连接，输出"孙辈 祖辈"关系。
