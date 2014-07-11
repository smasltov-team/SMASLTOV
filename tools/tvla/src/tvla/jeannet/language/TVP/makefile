all: TVPLex.java parser.java

TVPLex.java: TVP.lex
	java JLex.Main TVP.lex
	mv TVP.lex.java TVPLex.java

parser.java: TVP.cup
	java java_cup.Main < TVP.cup

