set JAVA_HOME=C:\don\runtime\java\jdk8u221x64
call ..\solr-7.3.0\bin\solr.cmd start -p 9876

@REM following works as relative on an external drive
@REM set JAVA_HOME=%CD:~0,2%\comp\runtime\jdk1.8.0_201.win