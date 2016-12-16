#!/bin/sh
acsPID=0

getAcsPID(){
    javaps=`ps -ef | grep acs-*.jar | grep -v "$0" | grep -v "grep"`
    if [ -n "$javaps" ]; then
        acsPID=`echo $javaps | awk '{print $2}'`
    else
        acsPID=0
    fi
}

shutdown(){
    getAcsPID
    echo "================================================================================================================"
    if [ $acsPID -ne 0 ]; then
        echo "Stopping ACS(PID=$acsPID)..."
        kill -9 $acsPID
        if [ $? -eq 0 ]; then
            echo "ACS stopped successful!"
            echo "================================================================================================================"
        else
            echo "ACS stopped failed!"
            echo "================================================================================================================"
        fi
        getAcsPID
        if [ $acsPID -ne 0 ]; then
            shutdown
        fi
    else
        echo "ACS is not running"
        echo "================================================================================================================"
    fi
}

shutdown
