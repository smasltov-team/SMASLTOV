package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.logic.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

public class LetExpressionAST extends ExpressionAST {
    Symbol var;
    ExpressionAST expr1;
    ExpressionAST expr2;

    /** Constructor */
    public LetExpressionAST(Symbol v, ExpressionAST expr1, ExpressionAST expr2) {
	this.var = v;
	this.expr1 = expr1;
	this.expr2 = expr2;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){
	expr1.substitute(from,to);
	expr2.substitute(from,to);
    }
    public void substitute(List froms, List tos){
	expr1.substitute(froms,tos);
	expr2.substitute(froms,tos);
    }

    /** Copy */
     public AST copy(){
	 return new LetExpressionAST(var, (ExpressionAST)expr1.copy(), (ExpressionAST)expr2.copy());
     }

    /** Evaluate Foreach intructions */
    public void evaluate(){
	expr1.evaluate();
	expr2.evaluate();
    }

    /** Generate an Expression */
    public Expression getExpression(){
	return new LetExpression(var, expr1.getExpression(),expr2.getExpression());
    }
}
