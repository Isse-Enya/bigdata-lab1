# 实验一 Linux 常用命令操作记录

以下命令均在 Ubuntu 24.04.2 LTS 的 bash 终端中执行，提示符中的 `isse` 为本次实验的登录用户。

## 1. 目录切换 cd

```bash
cd /usr/local                 # 切换到 /usr/local
cd ..                         # 切换到当前目录的上一级目录
cd ~                          # 切换回当前用户的主文件夹(/home/isse)
pwd
```

## 2. 查看文件与目录 ls

```bash
ls /usr                       # 查看 /usr 下的所有文件和目录
ls -l /usr                    # 以长格式显示权限、属主、大小、时间
ls -la /tmp                   # 查看 /tmp 下包括隐藏文件在内的全部内容
```

## 3. 新建目录 mkdir

```bash
cd /tmp
mkdir a                       # 在 /tmp 下创建目录 a
ls -ld /tmp/a
mkdir -p a1/a2/a3/a4          # 一次创建多级目录
```

## 4. 删除空目录 rmdir

```bash
rmdir /tmp/a                  # 删除空目录 a
rmdir -p a1/a2/a3/a4          # 由内向外逐级删除 a1/a2/a3/a4
ls /tmp
```

## 5. 复制文件或目录 cp

```bash
sudo cp ~/.bashrc /usr/bashrc1        # 复制并重命名
mkdir /tmp/test
sudo cp -r /tmp/test /usr/            # 递归复制目录
```

## 6. 移动与更名 mv

```bash
sudo mv /usr/bashrc1 /usr/test/       # 移动文件
sudo mv /usr/test /usr/test2          # 目录更名
```

## 7. 删除文件与目录 rm

```bash
sudo rm /usr/test2/bashrc1            # 删除文件
sudo rm -r /usr/test2                 # 递归删除目录
```

## 8. 查看文件内容 cat / tac / more

```bash
cat ~/.bashrc                         # 正向显示全部内容
tac ~/.bashrc                         # 反向显示
more ~/.bashrc                        # 分页显示，空格翻页，q 退出
```

## 9. 取前几行 head 与取后几行 tail

```bash
head -20 ~/.bashrc                    # 前 20 行
head -n -50 ~/.bashrc                 # 不显示最后 50 行
tail -20 ~/.bashrc                    # 最后 20 行
tail -n +51 ~/.bashrc                 # 从第 51 行开始显示
```

## 10. 文件时间 touch 与属主 chown

```bash
touch /tmp/hello
ls -l /tmp/hello
touch -d "5 days ago" /tmp/hello      # 时间改为 5 天前
ls -l /tmp/hello
sudo chown root /tmp/hello            # 属主改为 root
ls -l /tmp/hello
```

## 11. 文件查找 find

```bash
find ~ -name .bashrc
sudo find / -name .bashrc 2>/dev/null
```

## 12. 打包压缩 tar

```bash
sudo mkdir /test
sudo tar -zcvf /test.tar.gz /test     # 打包压缩
sudo tar -zxvf /test.tar.gz -C /tmp   # 解压到 /tmp
ls -ld /tmp/test
```

## 13. 查找字符串 grep

```bash
grep examples ~/.bashrc
grep -n examples ~/.bashrc
```

## 14. Hadoop 与 HDFS 的启动及基本操作

```bash
export HADOOP_HOME=$HOME/hadoop
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop

start-dfs.sh
start-yarn.sh
jps

hdfs dfs -mkdir -p /user/hadoop
hdfs dfs -mkdir -p /user/hadoop/test
hdfs dfs -ls /user/hadoop
hdfs dfs -put ~/.bashrc /user/hadoop/test
hdfs dfs -ls /user/hadoop/test
hdfs dfs -get /user/hadoop/test /usr/local/hadoop
ls -l /usr/local/hadoop/test
```
