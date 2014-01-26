#!/bin/bash

hadoop fs -rmr make-echos ; hadoop fs -copyFromLocal ../Makefiles/echos/ make-echos ; mvn clean install jar:jar
cd make-echos
hadoop jar ../make-0.0.1-SNAPSHOT.jar hadoop_playground.make.Make . all.txt
echo "OUTPUT:"
cat all.txt
