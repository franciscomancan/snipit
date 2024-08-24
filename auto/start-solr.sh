#!/bin/sh

cd ../solr-7.3.0/bin/

export JAVA_HOME=../../../runtime/jdk1.8.0_201.linux/

./solr start -p 9876