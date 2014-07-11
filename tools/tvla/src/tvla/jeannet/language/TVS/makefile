all: TVSLex.java parser.java

TVSLex.java: TVS.lex
	java JLex.Main TVS.lex
	mv TVS.lex.java TVSLex.java

parser.java: TVS.cup
	java java_cup.Main < TVS.cup

