# HDFS 十项操作的 Shell 命令实现

与 `HdfsOps.java` 中 Java API 实现的功能一一对应，命令均在启动 HDFS 后执行。

## 1. 上传文件，目标存在时由用户选择追加或覆盖

```bash
hdfs dfs -test -e /user/hadoop/data/local.txt \
  && read -p "文件已存在，追加(a)还是覆盖(o)?" choice \
  && if [ "$choice" = "a" ]; then hdfs dfs -appendToFile local.txt /user/hadoop/data/local.txt; \
     else hdfs dfs -put -f local.txt /user/hadoop/data/local.txt; fi \
  || hdfs dfs -put local.txt /user/hadoop/data/local.txt
```

## 2. 下载文件，本地重名时自动重命名

```bash
hdfs dfs -get /user/hadoop/data/local.txt ./download/
ls ./download/
```

## 3. 把指定文件内容输出到终端

```bash
hdfs dfs -cat /user/hadoop/data/local.txt
```

## 4. 显示文件的读写权限、大小、创建时间、路径

```bash
hdfs dfs -ls -h /user/hadoop/data/local.txt
hdfs dfs -stat "%A %b %y %n" /user/hadoop/data/local.txt
```

## 5. 递归显示目录下所有文件的详细信息

```bash
hdfs dfs -ls -R -h /user/hadoop
```

## 6. 创建与删除文件（父目录不存在时自动创建）

```bash
hdfs dfs -mkdir -p /user/hadoop/newdir
hdfs dfs -touchz /user/hadoop/newdir/newfile.txt
hdfs dfs -ls /user/hadoop/newdir
hdfs dfs -rm /user/hadoop/newdir/newfile.txt
```

## 7. 创建与删除目录（为空才删除）

```bash
hdfs dfs -mkdir -p /user/hadoop/a/b/c
hdfs dfs -ls -R /user/hadoop/a
hdfs dfs -rmdir /user/hadoop/a/b/c
hdfs dfs -rmdir /user/hadoop/a/b        # 非空目录不会被删除
```

## 8. 在文件开头或结尾追加内容

```bash
echo "append at tail" | hdfs dfs -appendToFile - /user/hadoop/data/local.txt

# HDFS 不支持直接在文件头部插入，需要先取回本地处理后再上传覆盖
hdfs dfs -get /user/hadoop/data/local.txt /tmp/local.txt
{ echo "append at head"; cat /tmp/local.txt; } > /tmp/new.txt
hdfs dfs -put -f /tmp/new.txt /user/hadoop/data/local.txt
hdfs dfs -cat /user/hadoop/data/local.txt
```

## 9. 删除指定文件

```bash
hdfs dfs -rm /user/hadoop/data/local.txt
```

## 10. 移动文件

```bash
hdfs dfs -mv /user/hadoop/data/local.txt /user/hadoop/data/moved.txt
hdfs dfs -ls /user/hadoop/data
```
