#!/bin/sh

HOSTLIST="hadoop100 hadoop101 hadoop102"
JAVA_HOME="/opt/module/jdk1.8.0_144"

for host in ${HOSTLIST}; do
  echo ======================= $host =============================
  ssh $host "source /etc/profile;${JAVA_HOME}/bin/jps" | grep -v Jps
done
