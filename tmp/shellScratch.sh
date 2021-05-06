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
    ssh $i ps -ef | grep $es_home | grep -v grep | awk '{print \$2}' | xargs kill >/dev/null 2>&1
  done
} ;;
esac

#======================================================================================================

#! /bin/sh
host_list="hadoop100 hadoop101 hadoop102"

case $1 in
"start") {
  for i in $host_list; do
    echo " ========= 启动 $i 采集flume ========="
    ssh $i "source /etc/profile;
                nohup $FLUME_HOME/bin/flume-ng agent --conf-file $FLUME_HOME/myJob/log-flume-kafka.conf --name a1
                -Dflume.root.logger=INFO,LOGFILE > $FLUME_HOME/logs/serverLog/log-flume-kafka.log 2>&1 &"
    echo -- Done ---
  done
} ;;
"stop") {
  for i in $host_list; do
    echo " ========= 停止 $i 采集flume ========="
    ssh $i "ps -ef | grep log-flume-kafka | grep -v grep |awk  '{print \$2}' | xargs kill -9 "
    echo --- Done ---
  done

} ;;
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

#! /bin/bash

host_list="hadoop100 hadoop101 hadoop102"
flume="$FLUME_HOME/bin"
job="$FLUME_HOME/myJob/kafka-file-hdfs.conf"
conf="$FLUME_HOME/conf"
log="$FLUME_HOME/logs/serverLog/log-flume-kafka.log"
kill="kafka-file-hdfs"
case $1 in
"start") {
  for i in $host_list; do
    echo " ========= 启动 $i 采集flume ========="
    ssh $i "source /etc/profile.d/my_env.sh;nohup $flume/flume-ng agent -n a1 -c $conf -f $job -Dflume.root.logger=INFO,console> $log 2>&1 &"

    echo -- Done ---
  done
} ;;
"stop") {
  for i in $host_list; do
    echo " ========= 停止 $i 采集flume ========="
    ssh $i "ps -ef | grep $kill | grep -v grep |awk  '{print \$2}' | xargs kill -9 "
    echo --- Done ---
  done

} ;;
esac

#!/bin/sh

case $1 in
"start") {
  echo ================== 启动 集群 ==================

  #启动 Zookeeper集群
  echo ---- start Zookeeper ----
  zk start

  zkPid1=$(ssh hadoop100 "ps -ef | grep -v grep | grep zookeeper | awk '{print \$2}'")
  zkPid2=$(ssh hadoop101 "ps -ef | grep -v grep | grep zookeeper | awk '{print \$2}'")
  zkPid3=$(ssh hadoop102 "ps -ef | grep -v grep | grep zookeeper | awk '{print \$2}'")
  sleep 4s

  if [ "$zkPid1" != "" -a "$zkPid2" != "" -a "$zkPid2" != "" ]; then
    #启动 Hadoop集群
    echo ---- start Hadoop ----
    hp start
    echo
    echo
  fi

  #启动 Kafka采集集群
  echo ---- start Kafka ----
  kfk start
  echo
  echo

  kfkPid1=$(ssh hadoop100 "ps -ef | grep -v grep | grep kafka | awk '{print \$2}'")
  kfkPid2=$(ssh hadoop101 "ps -ef | grep -v grep | grep kafka | awk '{print \$2}'")
  kfkPid3=$(ssh hadoop102 "ps -ef | grep -v grep | grep kafka | awk '{print \$2}'")
  sleep 4s

  if [ "$kfkPid1" != "" -a "$kfkPid2" != "" -a "$kfkPid2" != "" ]; then
    #启动 Flume采集集群
    echo ---- start Flume gather log ----
    lfk.sh start
    echo
    echo

    #启动 Flume消费集群
    kfh.sh start
    echo
    echo
  fi

} ;;

\
  "stop") {
    echo ================== 停止 集群 ==================

    #停止 Flume消费集群
    echo ---- stop Flume ----
    kfh.sh stop

    #停止 Flume采集集群
    lfk.sh stop

    #停止 Kafka采集集群
    echo ---- Stop Kafka ----
    kfk stop

    #停止 Hadoop集群
    echo ---- Stop Hadoop ----
    hp stop

    #停止 Zookeeper集群
    echo ---- Stop Zookeeper ----
    zk stop

  } ;;
esac

#!/bin/bash

RED_COLOR='\E[1;31m'
RES='\E[0m'

case $1 in
"start")
  echo ===== 启日志动采集 =====
  echo --- logger.sh ---
  a1_logger.sh start
  echo --- lfk.sh ---
  a3_lfk.sh start
  echo --- kfh.sh ---
  a4_kfh.sh start
  ;;
"stop")
  echo ===== 关闭日志采集 ====
  echo --- kfh.sh ---
  a4_kfh.sh stop
  echo --- lfk.sh ---
  a3_lfk.sh stop
  echo -e "${RED_COLOR} --- logger.sh需手动关闭 --- ${RES}"

  ;;
esac

#!/bin/bash

file="/opt/software/Mock/business/application.properties"
jar="/opt/software/Mock/business/gmall2020-mock-db-2020-05-18.jar"

if [ -n "$1" -a "$2" ]; then
  do_date=$1
  do_clear=$2

  echo ========== Start Generating Busibess Data ==========
  cd /opt/software/Mock/business
  sed -i /mock.date/cmock.date=$do_date $file
  sed -i /mock.clear/cmock.clear=$do_clear $file
  java -jar $jar

  echo ---- Done ----
elif [ -n "$1" ]; then
  do_date=$1
  echo ========== Start Generating Busibess Data ==========
  cd /opt/software/Mock/business
  sed -i /mock.date/cmock.date=$do_date $file
  sed -i /mock.clear/cmock.clear=0 $file
  java -jar $jar
  echo ---- Done ----
else
  do_date=$(date -d '-1 day' +%F)
  echo ========== Start Generating Busibess Data ==========
  cd /opt/software/Mock/business
  sed -i /mock.date/cmock.date=$do_date $file
  java -jar $jar
  echo ---- Done ----
fi



#! /bin/bash

DATAX_HOME=/opt/module/datax

#********************************************************
#* Global defines start.                                *
#********************************************************

MYSQL_HOST="192.168.1.100"          # 数据库地址
MYSQL_PORT=3306                     # 数据库端口
MYSQL_USER=root                     # 数据库用户
MYSQL_PASS="mysql"                  # 数据库密码
MYSQL_DABS=gmall                    # 数据库schema
#MYSQL_TABL='["user_date_20170929","user_date_20170930"]'
MYSQL_TABL=ALL                      # 如果是数据库下的所有表则写 ALL 即可
MYSQL_WHERE='""'                    # 抽取数据的where条件
MYSQL_COLUMN="['*']"                # 抽取的字段，如果是所有字段就填 *


HDFS_COLUMN='[{"id":"bigint"},{"id_type":"string"},{"last_date":"date"},{"first_date":"date"},{"last_read_date":"date"},{"first_read_date":"date"}]'     # hive中对应的字段
HDFS_FILENAME='mmb_monitor_snapshot'                        # hive中对应的schema
HDFS_PATH='/home/dataX_test'    # hive中对应的路径

HIVE_DB='mmb_monitor_snapshot'
HIVE_TB='mmb_user_snapshot'

JSON_OUT=/usr/local/datax/job/${HIVE_TB}.json               # 生成的json文件的路径


#********************************************************
#* Global defines end.                                  *
#********************************************************


# 当抽取所有表时，将表名解析成列表

TB_TEMP=""

if [ ${MYSQL_TABL} = "ALL" ] ; then
    for tb in `mysql -N -h${MYSQL_HOST} -u${MYSQL_USER} -p"${MYSQL_PASS}" -P${MYSQL_PORT} ${MYSQL_DABS} -e"SHOW TABLES;"`
    do
        TB_TEMP="\"${tb}\",${TB_TEMP}"
    done
fi

MYSQL_TABL="[${TB_TEMP}]"


# 调用python脚本

python json_maker.py \
        --output_json_path ${JSON_OUT} \
        --mysql_jdbc_url "['jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DABS}']" \
        --mysql_tables   ${MYSQL_TABL}   \
        --mysql_user     ${MYSQL_USER}   \
        --mysql_pass     ${MYSQL_PASS}   \
        --mysql_column   ${MYSQL_COLUMN} \
        --mysql_where    ${MYSQL_WHERE}  \
        --hdfs_column    ${HDFS_COLUMN}  \
        --hdfs_fileName  ${HDFS_FILENAME}\
        --hdfs_path      ${HDFS_PATH}




# 可以事先清空HIVE表
#hive -e "truncate ${HIVE_DB}.${HIVE_TB}"

# 执行datax，开始数据导入
python ${DATAX_HOME}/bin/datax.py  ${JSON_OUT}
