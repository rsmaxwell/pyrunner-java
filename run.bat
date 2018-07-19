@echo off

set x=target\classes
set x=%x%;%USERPROFILE%\.m2\repository\org\json\json\20180130\json-20180130.jar
set x=%x%;%USERPROFILE%\.m2\repository\net\java\dev\jna\jna-platform\4.3.0\jna-platform-4.3.0.jar
set x=%x%;%USERPROFILE%\.m2\repository\net\java\dev\jna\jna\4.3.0\jna-4.3.0.jar

set CLASSPATH=%x%

@echo on
java -classpath %CLASSPATH% com.rsmaxwell.pyrunner.Main
