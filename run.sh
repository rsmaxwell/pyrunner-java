#!/bin/bash

x=target/pyrunner-0.0.1-SNAPSHOT.jar
x=${x}:${HOME}/.m2/repository/org/json/json/20180130/json-20180130.jar

set -x
java -classpath ${x} com.rsmaxwell.pyrunner.Main
