#!/usr/bin/bash

x=target/classes
x=${x};${HOME}.m2/repository/org/json/json/20180130/json-20180130.jar
CLASSPATH=${x}

java -classpath ${CLASSPATH} com.rsmaxwell.pyrunner.Main
