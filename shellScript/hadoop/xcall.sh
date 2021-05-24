#!/bin/bash

RED_COLOR='\E[1;31m'
RES='\E[0m'

HOSTNAME=hadoop
H0=100
H1=104

# 判断输入参数个数
if [ $# -lt 1 ]; then
  echo -e "${RED_COLOR}not enough argument!${RES}"
  exit
fi


for (( i = H0; i <= H1; i++ )); do
    echo ==== $HOSTNAME$i ====
    ssh $HOSTNAME$i "source /etc/profile; $*"
    echo
done
