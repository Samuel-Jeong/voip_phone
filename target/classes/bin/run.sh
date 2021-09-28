#!/bin/sh
HOME=/home/voip_phone
PATH_TO_JAR=voip_phone-jar-with-dependencies.jar
SERVICE_NAME=voip_phone

JAVA_CONF=$HOME/voip_phone/config/
JAVA_OPT="-Dlogback.configurationFile=$HOME/voip_phone/config/logback.xml"
JAVA_OPT="$JAVA_OPT -Dio.netty.leakDetectionLevel=simple -Djdk.nio.maxCachedBufferSize=262144 -Dio.netty.allocator.type=unpooled"
JAVA_OPT="$JAVA_OPT -Dio.netty.noUnsafe=true -Dio.netty.noPreferDirect=true"
JAVA_OPT="$JAVA_OPT -XX:+UseG1GC -XX:G1RSetUpdatingPauseTimePercent=5 -XX:MaxGCPauseMillis=500 -XX:+UseLargePagesInMetaspace -XX:+UseLargePages -XX:+PrintGCDetails -verbosegc -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime -XX:+PrintPromotionFailure -XX:PrintFLSStatistics=1 -Xloggc:$HOME/voip_phone/logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M -Xms4G -Xmx4G"

if [ "$USER" != "voip_phone" ] ; then
	echo "Need to be application account(voip_phone)"
	exit 1
fi

checkfile()
{
	if [ ! -e $1 ]; then
		echo "$1" file does not exist.
		exit 2
	fi
}
checkdir()
{
	if [ ! -d $1 ]; then
		echo "$1" directory does not exist.
		exit 3
	fi
}

case $1 in
    start)
      checkdir $HOME/voip_phone/voip_phone/
      checkdir $HOME/voip_phone/voip_phone/bin
      checkdir $HOME/voip_phone/voip_phone/config
      checkdir $HOME/voip_phone/voip_phone/lib
      checkdir $HOME/voip_phone/voip_phone/logs

      checkfile $HOME/voip_phone/voip_phone/config/user_conf.ini
      checkfile $HOME/voip_phone/voip_phone/config/server_conf.ini
      checkfile $HOME/voip_phone/voip_phone/config/logback.xml

	if [ -f "$HOME/voip_phone/lib/$PATH_TO_JAR" ]; then
	  /usr/bin/java $JAVA_OPT $DEBUG -classpath $HOME/voip_phone/lib/$PATH_TO_JAR VoipPhoneMain $JAVA_CONF > /dev/null 2>&1 &
	  echo "$SERVICE_NAME started ..."
	  /usr/bin/logger -p info -t "$0" "voip_phone started"
	else
	  echo "(ERROR) start fail : $?"
	  exit 4
	fi
    ;;
    stop)
	PID=`ps -ef | grep java | grep VoipPhoneMain | awk '{print $2}'`
	if [ -z $PID ]
	then
		echo "voip_phone is not running"
	else
		echo "stopping voip_phone"
		kill $PID
		sleep 1
		PID=`ps -ef | grep java | grep VoipPhoneMain | awk '{print $2}'`
		if [ ! -z $PID ]
		then
			echo "kill -9"
			kill -9 $PID
		fi
		echo "voip_phone stopped"
		/usr/bin/logger -p info -t "$0" "AMF stopped"
	fi
    ;;
esac
