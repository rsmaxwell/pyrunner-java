#!/usr/bin/bash

x=target/classes
x=${x}:/cygdrive/c/Users/Richard/.m2/repository/org/json/json/20180130/json-20180130.jar
CLASSPATH=${x}

set -x
java -classpath ${CLASSPATH} com.rsmaxwell.pyrunner.Main
