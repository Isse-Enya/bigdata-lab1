#!/usr/bin/env bash
# 实验一 HDFS 常用操作：用 Hadoop 提供的 Shell 命令实现十项功能。
# 用法: bash hdfs_ops.sh <子命令> [参数...]
set -e

HDFS="hdfs dfs"

usage() {
  cat <<'EOF'
HDFS 常用操作 Shell 实现，用法：
  upload   <本地文件> <HDFS路径> [append|overwrite]
  download <HDFS文件> <本地目录>
  cat      <HDFS文件>
  info     <HDFS路径>
  lsr      <HDFS目录>
  mkfile   <HDFS文件>
  rmfile   <HDFS文件>
  mkdir    <HDFS目录>
  rmdir    <HDFS目录>
  append   <HDFS文件> <内容> <head|tail>
  rm       <HDFS路径>
  move     <源路径> <目的路径>
EOF
}

cmd=$1
shift || true

case "$cmd" in
  upload)
    # 功能 1：目标文件已存在时由用户选择追加(append)或覆盖(overwrite)
    local_file=$1
    remote=$2
    mode=${3:-}
    if $HDFS -test -e "$remote"; then
      if [ -z "$mode" ]; then
        read -r -p "文件 $remote 已存在，请选择 append(追加) 或 overwrite(覆盖): " mode
      fi
      if [ "$mode" = "append" ]; then
        $HDFS -appendToFile "$local_file" "$remote"
        echo "已把 $local_file 追加到 $remote 末尾"
      elif [ "$mode" = "overwrite" ]; then
        $HDFS -put -f "$local_file" "$remote"
        echo "已覆盖 $remote"
      else
        echo "未识别的选项: $mode"
      fi
    else
      $HDFS -put "$local_file" "$remote"
      echo "已上传 $local_file -> $remote"
    fi
    ;;
  download)
    # 功能 2：本地同名文件存在时自动重命名
    remote=$1
    dir=$2
    mkdir -p "$dir"
    name=$(basename "$remote")
    base=${name%.*}
    suffix=""
    [ "$base" != "$name" ] && suffix=".${name##*.}"
    target="$dir/$name"
    i=1
    while [ -e "$target" ]; do
      target="$dir/${base}_${i}${suffix}"
      i=$((i + 1))
    done
    $HDFS -get "$remote" "$target"
    echo "已下载 $remote -> $target"
    ;;
  cat)
    # 功能 3：输出文件内容到终端
    $HDFS -cat "$1"
    ;;
  info)
    # 功能 4：显示权限、大小、修改时间、路径
    $HDFS -ls -h "$1"
    $HDFS -stat "权限=%A 大小=%b 修改时间=%y 路径=%n" "$1"
    ;;
  lsr)
    # 功能 5：递归显示目录下所有文件的信息
    $HDFS -ls -R -h "$1"
    ;;
  mkfile)
    # 功能 6：创建文件，父目录不存在时自动创建
    $HDFS -mkdir -p "$(dirname "$1")"
    $HDFS -touchz "$1"
    echo "已创建文件 $1"
    ;;
  rmfile)
    # 功能 6 的删除部分
    $HDFS -rm "$1"
    ;;
  mkdir)
    # 功能 7：创建目录，父目录不存在时自动创建
    $HDFS -mkdir -p "$1"
    echo "已创建目录 $1"
    ;;
  rmdir)
    # 功能 7：目录为空时删除，非空时拒绝
    if [ -z "$($HDFS -ls "$1" 2>/dev/null | grep -v "^Found")" ]; then
      $HDFS -rmdir "$1"
      echo "已删除空目录 $1"
    else
      echo "目录 $1 非空，拒绝删除"
    fi
    ;;
  append)
    # 功能 8：在文件开头或结尾追加内容
    file=$1
    content=$2
    position=$3
    tmp=$(mktemp)
    if [ "$position" = "tail" ]; then
      printf '%s\n' "$content" | $HDFS -appendToFile - "$file"
      echo "已在 $file 末尾追加内容"
    elif [ "$position" = "head" ]; then
      rm -f "$tmp"
      $HDFS -get "$file" "$tmp"
      { printf '%s\n' "$content"; cat "$tmp"; } > "$tmp.new"
      $HDFS -put -f "$tmp.new" "$file"
      echo "已在 $file 开头写入内容"
    else
      echo "位置参数只能是 head 或 tail"
    fi
    rm -f "$tmp" "$tmp.new"
    ;;
  rm)
    # 功能 9：删除指定文件
    $HDFS -rm "$1"
    ;;
  move)
    # 功能 10：移动/重命名文件
    $HDFS -mkdir -p "$(dirname "$2")"
    $HDFS -mv "$1" "$2"
    echo "已把 $1 移动到 $2"
    ;;
  *)
    usage
    ;;
esac
