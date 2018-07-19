@echo off

set x=target\classes
set x=%x%;%USERPROFILE%\.m2\repository\org\json\json\20180130\json-20180130.jar
set CLASSPATH=%x%

java -classpath %CLASSPATH% com.rsmaxwell.pyrunner.Main
