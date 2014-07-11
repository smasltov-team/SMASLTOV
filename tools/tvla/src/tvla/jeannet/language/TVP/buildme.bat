@echo off
REM ********************************************************
REM A Win32 script to generate the TVP lnaguage parser and
REM scanner.
REM Runs the Lex and Cup on TVP.lex and TVP.cup respectively
REM ********************************************************

java JLex.Main TVP.lex
copy TVP.lex.java TVPLex.java
del TVP.lex.java
java java_cup.Main < TVP.cup
