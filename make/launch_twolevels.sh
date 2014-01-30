#!/bin/bash
EXAMPLE="twolevels"

hadoop fs -rmr make-${EXAMPLE} ; hadoop fs -copyFromLocal ../Makefiles/${EXAMPLE}/ make-${EXAMPLE} ; mvn clean install jar:jar
hadoop jar make-0.0.1-SNAPSHOT.jar hadoop_playground.make.Make make-${EXAMPLE} all.txt
echo ""
echo "OUTPUT:"
hadoop fs -cat make-${EXAMPLE}/all.txt
