#!/bin/bash

hadoop fs -rmr make-echos ; hadoop fs -copyFromLocal ../Makefiles/echos/ make-echos ; mvn clean install jar:jar
cd ../Makefiles/make-echos
hadoop jar ../make/make-0.0.1-SNAPSHOT.jar hadoop_playground.make.Make make-echos all.txt
echo "OUTPUT:"
cat all.txt
