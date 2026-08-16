@echo off
set /p CP=<cp.txt
java -cp "%CP%;target/classes;target/test-classes" com.campusguide.EmbeddedMongoRunner
