#!/bin/sh

RED_COLOR='\E[1;31m'
RES='\E[0m'

HOSTLIST="hadoop100 hadoop101 hadoop102"

# 判断输入参数个数
if [ $# -lt 1 ]; then
  echo -e "${RED_COLOR}not enough argument!${RES}"
  exit
fi

for host in $HOSTLIST; do
  echo ====== ${host} ======

  for file in "$@"; do
#    判断文件是否存在
    if [ -e ${file} ]; then
#      获取父目录
      pdir=$(cd -P $(dirname $file); pwd)

#      获取当前文件名称
      fname=$(basename ${file})
      ssh ${host} "mkdir -p ${pdir}"
      rsync -av ${pdir}/${fname} ${host}:${pdir}
    else
      echo -e "${RED_COLOR} ${file} does not exit!${RES}"
    fi
  done
done


kafka-console-consumer.sh \
--bootstrap-server hadoop100:9092 \
--formatter "kafka.coordinator.group.GroupMetadataManager\$OffsetsMessageFormatter" \
--topic __consumer_offsets \
--consumer-property exclude.internal.topics=false \
--from-beginning