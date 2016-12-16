#!/bin/sh

JAVA_OPTS="-Xmx6G -Xms6G -XX:NewSize=256m -XX:MaxNewSize=256m -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=2 -XX:TargetSurvivorRatio=50 -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:+UseStringDeduplication"

acsPID=0

getAcsPID(){
    javaps=`ps -ef | grep acs-*.jar | grep -v "$0" | grep -v "grep"`
    if [ -n "$javaps" ]; then
        acsPID=`echo $javaps | awk '{print $2}'`
    else
        acsPID=0
    fi
}

startup(){
    getAcsPID
    echo "================================================================================================================"
    if [ $acsPID -ne 0 ]; then
        echo "ACS already started(PID=$acsPID)!"
        echo "================================================================================================================"
    else
        echo "Starting ACS..."
        set CLASSPATH=.
        nohup java $JAVA_OPTS -Xloggc:gc.log  -jar acs-*.jar >server.out 2>&1 &
        getAcsPID
        if [ $acsPID -ne 0 ]; then
            echo "ACS started successful(PID=$acsPID)!"
            echo "================================================================================================================"
        else
            echo "ACS started  failed!"
            echo "================================================================================================================"
        fi
    fi
}

startup

