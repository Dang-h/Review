#!/bin/sh

RED_COLOR='\E[1;31m'
RES='\E[0m'

PRESTO_HOME='/opt/module/presto-server-0.233.1'

function check_process() {
  pid=$(ps -ef 2>/dev/null | grep -v grep | grep -i "$1" | awk '{print$2}')
  echo $pid
  [[ $pid ]] && return 1 || return 0
}

METASTORE_PID=$(check_process hive-metastore)
PRESTO_PID=$(check_process PrestoServer)

function start() {

  if [ ! $METASTORE_PID ]; then
    echo "=================== 启动Hive Metastore =================== "
    nohup ${HIVE_HOME}/bin/hive --service metastore >${PRESTO_HOME}/logs/hiveMetastore.log 2>&1 &
  else
    echo "Hive Metastore已启动"
  fi

  if [ ! $PRESTO_PID ]; then
    echo "=================== 启动Presto =================== "
    for i in hadoop{100..102}; do
      ssh $i ${PRESTO_HOME}/bin/launcher start
    done
  else
    echo "Presto已启动"
  fi

}

function stop() {

if [ ${PRESTO_PID} ]; then
    echo "=================== 关闭Presto =================== "
    for i in hadoop{100..102}; do
      ssh $i ${PRESTO_HOME}/bin/launcher stop
    done
  else
    echo "Presto未启动"
  fi

  if [[ ${METASTORE_PID} ]]; then
    echo "=================== 关闭Hive Metastore =================== "
    kill -9 $METASTORE_PID
  else
    echo "Hive Metastore未启动"
  fi

}

case $1 in
"start")
  start
  ;;
"stop")
  stop
  ;;
"status")
  cmd_hiveMetastore="echo hiveMetastore=${METASTORE_PID}"
  cmd_presto="echo presto=${PRESTO_PID}"
  [ ${METASTORE_PID} ] && eval ${cmd_hiveMetastore}  && [ ${PRESTO_PID} ] && eval ${cmd_presto} && echo "服务正常" || echo "服务异常"
  ;;
*)
  echo -e "${RED_COLOR}Argument Error!${RES}"
  echo 'Usage: '$(basename $0)' start|stop|status'
  ;;
esac
