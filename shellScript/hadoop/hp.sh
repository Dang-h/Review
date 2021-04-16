#!/bin/sh

RED_COLOR='\E[1;31m'
RES='\E[0m'

HADOOP_HOME='/opt/module/hadoop-3.1.3'

# 判断输入参数个数
if [ $# -lt 1 ]; then
  echo -e "${RED_COLOR}Argument Error!${RES}"
  exit
fi

case $1 in
"start")
  echo "=================== 启动HDFS =================== "
  ${HADOOP_HOME}/start-dfs.sh
  echo
  echo "Done"

  echo "=================== 启动YARN =================== "
  ${HADOOP_HOME}/sbin/start-yarn.sh
  echo
  echo "Done"

  echo "=================== 启动JobHistoryServer =================== "
  ${HADOOP_HOME}sbin/mapred --daemon start historyserver
  echo
  echo "Done"
  ;;
"stop")
  echo "=================== 关闭YARN =================== "
  ${HADOOP_HOME}/sbin/stop-yarn.sh
  echo
  echo "Done"

  echo "=================== 关闭HDFS =================== "
  ${HADOOP_HOME}/sbin/stop-dfs.sh
  echo
  echo "Done"

  echo "=================== 关闭JpbHistoryServer =================== "
  ${HADOOP_HOME}/sbin/mapred --daemon stop historyserver
  echo
  echo "Done"
  ;;
esac



