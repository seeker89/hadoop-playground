# Hadoop Playground

Using Hadoop to get distributed, parallel make


## Getting started

To get started, get Hadoop 1.2.1 tar.gz from one of the available mirrors.

Whilst downloading, take a look at some docs

Hadoop principles and tutorial
* [https://docs.marklogic.com/guide/mapreduce/hadoop](https://docs.marklogic.com/guide/mapreduce/hadoop)

Hadoop & Eclipse
* [http://blogs.igalia.com/dpino/2012/10/14/starting-with-hadoop/](http://blogs.igalia.com/dpino/2012/10/14/starting-with-hadoop/)
* [https://github.com/data-tsunami/hello-hadoop](https://github.com/data-tsunami/hello-hadoop)
* [https://github.com/dpino/Hadoop-Word-Count](https://github.com/dpino/Hadoop-Word-Count)
* [http://www.drdobbs.com/database/hadoop-writing-and-running-your-first-pr/240153197?pgno=1](http://www.drdobbs.com/database/hadoop-writing-and-running-your-first-pr/240153197?pgno=1)

Hadoop dev mode
* [http://blog.tundramonkey.com/2013/02/24/setting-up-hadoop-on-osx-mountain-lion](http://blog.tundramonkey.com/2013/02/24/setting-up-hadoop-on-osx-mountain-lion)
* [http://importantfish.com/how-to-run-hadoop-in-standalone-mode-using-eclipse-on-mac-os-x/](http://importantfish.com/how-to-run-hadoop-in-standalone-mode-using-eclipse-on-mac-os-x/)
* [http://wiki.apache.org/hadoop/WordCount](http://wiki.apache.org/hadoop/WordCount)



Mini cluster:
* setting up mini cluster [http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/CLIMiniCluster.html](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/CLIMiniCluster.html)

General:
* setting up the environment [http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html)
* intro with screenshots [http://hortonworks.com/hadoop-tutorial/hello-world-an-introduction-to-hadoop-hcatalog-hive-and-pig/](http://hortonworks.com/hadoop-tutorial/hello-world-an-introduction-to-hadoop-hcatalog-hive-and-pig/)
* Apache map-reduce tutorial [https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html](https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html)
* very old, but clears few things out [http://www.michael-noll.com/tutorials/running-hadoop-on-ubuntu-linux-single-node-cluster/](http://www.michael-noll.com/tutorials/running-hadoop-on-ubuntu-linux-single-node-cluster/)

Streaming API:
* [http://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/](http://www.michael-noll.com/tutorials/writing-an-hadoop-mapreduce-program-in-python/)


## Compiling the example

In the folder "make" there is a maven/eclipse project, using Hadoop 1.2.1. You can import it with eclipse. It's the word count example.
You will need Hadoop 1.2.1 up and running.

To compile, just use maven:

    mvn clean compile jar:jar

Let's test it:
    
    # put some text in the file
    echo "hello moto, how are you ? hadoop hadoop" > test.txt
    hadoop fs -mkdir texts 
    hadoop fs -copyFromLocal test.txt texts
    
    # run the job
    hadoop jar make-0.0.1-SNAPSHOT.jar hadoop_playground.make.Make texts output
    
    # check the output in the
    hadoop fs -ls output
    hadoop fs -cat output/part-r-00000


## Using Hadoop for the distributed make





