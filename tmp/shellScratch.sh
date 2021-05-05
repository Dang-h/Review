#!/bin/bash

# 通过Nginx将日志采集到不同的Kafka
JAVA_BIN=$JAVA_HOME/bin/java
APPNAME=logger-0.0.1-SNAPSHOT.jar

case $1 in
"start")
  {
    for i in hadoop100 hadoop101 hadoop102; do
      echo "========: $i==============="
      ssh $i "${JAVA_BIN} -Xms32m -Xmx64m -jar /home/hadoop/jar/${APPNAME} >/dev/null 2>&1 &"
    done
    echo "========NGINX==============="
    /opt/module/nginx-1.12.2/sbin/nginx
  }
  ;;

"stop")
  {
    echo "======== NGINX==============="
    /opt/module/nginx-1.12.2/sbin/nginx -s stop

    for i in hadoop100 hadoop101 hadoop102; do
      echo "========: $i==============="
      ssh $i "ps -ef|grep ${APPNAME}|grep -v grep|awk '{print \$2}'|xargs kill" >/dev/null 2>&1
    done
  }
  ;;
esac

#======================================================================================================

#!/bin/sh

# 生成指定日期的模拟日志
dateTime=$(date "+%Y-%m-%d")

if [ $1 ]; then
  dateTime=$1
fi

function check_process() {
  pid=$(ps -ef 2>/dev/null | grep -v grep | grep -i "$1" | awk '{print$2}')
  echo $pid
  [ $pid ] && return 1 || return 0
}

function make_log() {
  logger=$(check_process logger-0.0.1-SNAPSHOT.jar)
  cmd="cd /opt/software/Mock/log;
        sed -i /^mock.date/cmock.date=${dateTime} application.properties;
        java -jar gmall2020-mock-log-2020-05-10.jar >/dev/null 2>&1 &"

  [ $logger ] && eval $cmd || echo "---采集未启动---"
}

make_log

#======================================================================================================

#!/bin/bash
es_home=/opt/module/elasticsearch-6.8.1
HostList="hadoop100 hadoop101 hadoop102"

case $1 in
"start") {
  for i in $HostList; do
    echo "==============$i 上 ES 启动=============="
    ssh $i ${es_home}/bin/elasticsearch >/dev/null 2>&1 &
  done
} ;;
"stop") {
  for i in $HostList; do
    echo "==============$i 上 ES 停止=============="
    ssh $i ps -ef|grep $es_home |grep -v grep|awk '{print \$2}'|xargs kill >/dev/null 2>&1
  done
} ;;
esac

#======================================================================================================

#! /bin/sh
host_list="hadoop100 hadoop101 hadoop102"

case $1 in
"start"){
        for i in $host_list
        do
                echo " ========= 启动 $i 采集flume ========="
                ssh $i "source /etc/profile;
                nohup $FLUME_HOME/bin/flume-ng agent --conf-file $FLUME_HOME/myJob/log-flume-kafka.conf --name a1
                -Dflume.root.logger=INFO,LOGFILE > $FLUME_HOME/logs/serverLog/log-flume-kafka.log 2>&1 &"
                echo -- Done ---
        done
};;
"stop"){
        for i in $host_list
        do
                echo " ========= 停止 $i 采集flume ========="
                ssh $i "ps -ef | grep log-flume-kafka | grep -v grep |awk  '{print \$2}' | xargs kill -9 "
                echo --- Done ---
        done

};;
esac


#!/bin/sh

dateTime=$(date "+%Y-%m-%d")

if [ $1 ]; then
  dateTime=$1
fi

function make_log() {
  cmd="cd /opt/software/Mock/log;
		   sed -i '/<property name="DATE" value=.*/c<property name=\"DATE\" value=\"${dateTime}\" \/>' logback.xml;
		   "

   eval $cmd
}

make_log
