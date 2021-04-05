#!/bin/sh

SPARKDIR="/opt/module/spark-3.0-yarn"

if [ $# -lt 1 ]; then
    echo "No Args Input..."
    exit;
fi

function check_process() {
  # 查进程号
  pid=$(ps -ef 2>/dev/null | grep -v grep | grep -i "$1" | awk '{print$2}')
  # 查端口号
  #ppid=$(netstat -nltp 2>/dev/null | grep "$2" | awk '{print$7}') | cut -d '/' -f 1

  echo $pid

  [ -z "$pid" ]  && return 1 || return 0
}

function sparkSql_start() {

  server2pid=$(check_process thriftserver)
  cmd="$SPARKDIR/sbin/start-thriftserver.sh"

  [ -z "$server2pid" ] && eval $cmd || echo "thriftserver 服务已启动"

}

function sparkSql_stop() {

  server2pid=$(check_process thriftserver)
  cmd="$SPARKDIR/sbin/stop-thriftserver.sh"

  if [ -n "$server2pid" ]; then
      eval $cmd
  else
    echo "thriftserver 服务未启动"
  fi

}


case $1 in
"start")
  echo "===== 启动sparkSQLService ====="
  sparkSql_start
  ;;
"stop")
  echo "===== 关闭sparkSQLService ====="
  sparkSql_stop
  ;;
"status")
  check_process thriftserver >/dev/null && echo "thriftserver 服务运行正常" || echo "thriftserver 服务运行异常"
  ;;
esac