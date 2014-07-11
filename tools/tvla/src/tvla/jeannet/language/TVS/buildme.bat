@echo off
REM ********************************************************
REM A Win32 script to generate the TVS lnaguage parser and
REM scanner.
REM Runs the Lex and Cup on TVS.lex and TVS.cup respectively
REM ********************************************************

if exist TVSLex.java del TVSLex.java
if exist parser.java del parser.java

echo Creating scanner class
java JLex.Main TVS.lex

echo Creating parser class
java java_cup.Main < TVS.cup

ren TVS.lex.java TVSLex.java
