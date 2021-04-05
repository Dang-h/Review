#!/bin/sh

HOSTNAME=hadoop
HOSTINDEX_0=100
HOSTINDEX_1=103
VERSION=kafka_2.11-2.4.1

case $1 in
	start )
		for((i=$HOSTINDEX_0; i<$HOSTINDEX_1; i++));
		do
			echo ===================$HOSTNAME$i===================
			ssh $HOSTNAME$i "source /etc/profile; /opt/module/$VERSION/bin/kafka-server-start.sh -daemon /opt/module/$VERSION/config/server.properties"
			echo
			echo Done
		done
		;;
	stop )

		for((i=$HOSTINDEX_0; i<$HOSTINDEX_1; i++));
		do
			echo ===================$HOSTNAME$i===================
			ssh $HOSTNAME$i "source /etc/profile; /opt/module/$VERSION/bin/kafka-server-stop.sh"
			echo
			echo Done
		done
		;;
esac
