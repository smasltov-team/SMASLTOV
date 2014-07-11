// @author Tal Lev-Ami.
// @since 9.5.2001 Added the characters '$' and '.' to identifiers (Roman).
package tvla.language.TVP;

import tvla.exceptions.*;
import tvla.util.*;
import java_cup.runtime.Symbol;
import java.util.*;
%%
%{
int line_count = 1;

void addProperties(String props) {
	try {
		int b = props.indexOf('\"')+1;
		int e = props.indexOf('\"', b);
		String filename = props.substring(b, e);
		ProgramProperties.load(filename);
	}
	catch (Exception e) {
		throw new UserErrorException("Unable to load properties file specified by " +
		props);
	}
}
%}

%eofval{
return (new Symbol(sym.EOF));
%eofval}

%cup
%implements java_cup.runtime.Scanner
%function next_token
%class TVPLex
%type Symbol

Letter = [a-zA-Z_$]
Digit = [0-9]
Number = {Digit}{Digit}*
Id = {Letter}({Letter}|{Digit}|\$|\.)*
String = \"[^\"]*\"
%state COMMENT
%state LINECOMMENT
%%

<YYINITIAL>"/*"	{yybegin(COMMENT) ;}
<YYINITIAL>"//"	{yybegin(LINECOMMENT) ;}
<YYINITIAL>"==>"   {return new Symbol(sym.IMPLIES_T);}
<YYINITIAL>"%p"	{return new Symbol(sym.PRED); }
<YYINITIAL>"%i"	{return new Symbol(sym.INS_PRED); }
<YYINITIAL>"%r"	{return new Symbol(sym.CONSISTENCY_RULE );}
<YYINITIAL>"%t"	{return new Symbol(sym.TITLE);}
<YYINITIAL>"%s"	{return new Symbol(sym.SET);}

<YYINITIAL>"%function"	{return new Symbol(sym.FUNCTION );}
<YYINITIAL>"begin"         {return new Symbol(sym.BEGIN );}
<YYINITIAL>"end"         {return new Symbol(sym.END );}
<YYINITIAL>"focus"         {return new Symbol(sym.FOCUS );}
<YYINITIAL>"precondition:"  {return new Symbol(sym.PRECOND_COLON );}
<YYINITIAL>"halt:"	{return new Symbol(sym.HALT_COLON );}
<YYINITIAL>"message:"	{return new Symbol(sym.MESSAGE_COLON );}
<YYINITIAL>"new:"	{return new Symbol(sym.NEW_COLON );}
<YYINITIAL>"clone:"	{return new Symbol(sym.CLONE_COLON );}
<YYINITIAL>"retain:"	{return new Symbol(sym.RETAIN_COLON );}
<YYINITIAL>"update:"	{return new Symbol(sym.UPDATE_COLON );}
<YYINITIAL>"transform"	{return new Symbol(sym.TRANSFORM );}
<YYINITIAL>"blur"	{return new Symbol(sym.BLUR );}
<YYINITIAL>"embedblur"	{return new Symbol(sym.EMBEDBLUR );}
<YYINITIAL>"join"	{return new Symbol(sym.JOIN );}
<YYINITIAL>"meetwith"	{return new Symbol(sym.MEETWITH );}
<YYINITIAL>"joinwith"	{return new Symbol(sym.JOINWITH );}
<YYINITIAL>"prefab"	{return new Symbol(sym.PREFAB );}
<YYINITIAL>"diff_over"	{return new Symbol(sym.DIFF_OVER );}
<YYINITIAL>"diff_under"	{return new Symbol(sym.DIFF_UNDER );}
<YYINITIAL>"meetforeach"	{return new Symbol(sym.MEETFOREACH );}
<YYINITIAL>"joinforeach"	{return new Symbol(sym.JOINFOREACH );}
<YYINITIAL>"coerce" {return new Symbol(sym.COERCE );}
<YYINITIAL>"let" {return new Symbol(sym.LET );}
<YYINITIAL>"in" {return new Symbol(sym.IN );}
<YYINITIAL>"copy" {return new Symbol(sym.COPY );}

<YYINITIAL>"auto"	{return new Symbol(sym.AUTO );}
<YYINITIAL>"foreach"	{return new Symbol(sym.FOREACH );}
<YYINITIAL>E       {return new Symbol(sym.EXISTS);}
<YYINITIAL>A       {return new Symbol(sym.FORALL);}
<YYINITIAL>TC       {return new Symbol(sym.TC);}
<YYINITIAL>\<[ ]?\-[ ]?\>	{return new Symbol(sym.IFF);}
<YYINITIAL>\-[ ]?\>	{return new Symbol(sym.IMPLIES);}
<YYINITIAL>"|"	{return new Symbol(sym.OR);}
<YYINITIAL>"&"	{return new Symbol(sym.AND);}
<YYINITIAL>"!"	{return new Symbol(sym.NOT);}
<YYINITIAL>"*"	{return new Symbol(sym.STAR);}
<YYINITIAL>"+"	{return new Symbol(sym.PLUS);}
<YYINITIAL>"-"	{return new Symbol(sym.MINUS);}
<YYINITIAL>"=="	{return new Symbol(sym.EQ);}
<YYINITIAL>"!="    {return new Symbol(sym.NEQ);}
<YYINITIAL>[\t ] {}
<YYINITIAL>"1/2" {return new Symbol(sym.UNKNOWN );}
<YYINITIAL>1    {return new Symbol(sym.TRUE);}
<YYINITIAL>0   {return new Symbol(sym.FALSE);}
<YYINITIAL>{String} {return new Symbol(sym.STRING, yytext().substring(1, yytext().length()-1));}
<YYINITIAL>{Id}	{return new Symbol(sym.ID, yytext()); }
<YYINITIAL>"["  {return new Symbol(sym.LBR); }
<YYINITIAL>"]"  {return new Symbol(sym.RBR); }
<YYINITIAL>"("  {return new Symbol(sym.LP); }
<YYINITIAL>")"  {return new Symbol(sym.RP); }
<YYINITIAL>"{"  {return new Symbol(sym.LCBR); }
<YYINITIAL>"}"  {return new Symbol(sym.RCBR); }
<YYINITIAL>"="	{return new Symbol(sym.ASSIGN);}
<YYINITIAL>"?"	{return new Symbol(sym.QMARK);}
<YYINITIAL>","	{return new Symbol(sym.COMMA);}
<YYINITIAL>":"	{return new Symbol(sym.COLON);}
<YYINITIAL>";"	{return new Symbol(sym.SEMICOLON);}
<YYINITIAL>"%"	{return new Symbol(sym.PERCENT);}
<YYINITIAL>"/"	{return new Symbol(sym.COMBINE);}
<YYINITIAL>"-->"	{return new Symbol(sym.ARROW);}

<YYINITIAL>\r {}
<YYINITIAL>\n	{ line_count++;}
<YYINITIAL>\032 {}
<YYINITIAL>.   { Logger.println("Illegal character: "+yytext()); }
<COMMENT>"*/"	{ yybegin(YYINITIAL) ;}
<COMMENT>\n   {line_count++;}
<COMMENT>"@properties"[\t ]+{String} { addProperties(yytext()); }
<COMMENT>.   {}
<LINECOMMENT>\n   {line_count++; yybegin(YYINITIAL); }
<LINECOMMENT>"@properties"[\t ]+{String} { addProperties(yytext()); }
<LINECOMMENT>.   {}
