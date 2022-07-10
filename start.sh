#!/bin/bash
set -x;

deploypath=/home/ec2-user/java/deploy
date=`date +%d-%m-%Y`
JAVA_OPTS=" -server -Xms1024m -Xmx1524m -verbose:gc -XX:+PrintGCDetails -Xloggc:$deploypath/logs/gc.log -XX:HeapDumpPath=$deploypath/logs/`date +%Y_%m_%d_%H_%M`.hprof"
nohup java $JAVA_OPTS -jar $deploypath/game.jar /dev/null 2>&1 &