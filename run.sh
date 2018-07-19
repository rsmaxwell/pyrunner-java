#!/bin/bash

x=target/pyrunner-0.0.1-SNAPSHOT.jar
x=${x}:${HOME}/.m2/repository/org/json/json/20180130/json-20180130.jar
x=${x}:${HOME}/.m2/repository/net/java/dev/jna/jna-platform/4.5.2/jna-platform-4.5.2.jar
x=${x}:${HOME}/.m2/repository/net/java/dev/jna/jna/4.3.0/jna-4.5.2.jar

set -x
java -classpath ${x} com.rsmaxwell.pyrunner.Main
