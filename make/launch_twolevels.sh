#!/bin/bash
EXAMPLE="twolevels"

hadoop fs -rmr make-${EXAMPLE} ; hadoop fs -copyFromLocal ../Makefiles/${EXAMPLE}/ make-${EXAMPLE} ; mvn clean install jar:jar
cd make-${EXAMPLE}
hadoop jar ../make-0.0.1-SNAPSHOT.jar hadoop_playground.make.Make . all.txt
echo ""
echo "OUTPUT:"
cat all.txt
