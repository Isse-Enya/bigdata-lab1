#!/usr/bin/env bash
# 实验一一键运行脚本：编译源码，上传输入数据并依次运行三个 MapReduce 作业。
set -e

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export HADOOP_HOME=$HOME/hadoop
export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop

ROOT=$(cd "$(dirname "$0")" && pwd)
cd "$ROOT"

echo "== 1. 编译 =="
mkdir -p build
javac -encoding UTF-8 -cp "$(hadoop classpath)" -d build hdfs/HdfsOps.java mapreduce/*.java

echo "== 2. 准备 HDFS 目录与输入数据 =="
hdfs dfs -rm -r -f -skipTrash /lab1 >/dev/null 2>&1 || true
hdfs dfs -mkdir -p /lab1/input/dedup /lab1/input/sort /lab1/input/grandparent
hdfs dfs -put -f mapreduce/input/fileA.txt mapreduce/input/fileB.txt /lab1/input/dedup/
hdfs dfs -put -f mapreduce/input/file1.txt mapreduce/input/file2.txt mapreduce/input/file3.txt /lab1/input/sort/
hdfs dfs -put -f mapreduce/input/child-parent.txt /lab1/input/grandparent/
hdfs dfs -ls -R /lab1/input

echo "== 3. 运行 MapReduce 作业 =="
CLASSPATH="build:$(hadoop classpath)"
java -cp "$CLASSPATH" DedupMerge /lab1/input/dedup /lab1/output/dedup
java -cp "$CLASSPATH" SortNumbers /lab1/input/sort /lab1/output/sort
java -cp "$CLASSPATH" GrandParent /lab1/input/grandparent /lab1/output/grandparent

echo "== 4. 输出结果 =="
for name in dedup sort grandparent; do
  echo "----- $name -----"
  hdfs dfs -cat /lab1/output/$name/part-r-00000
done
