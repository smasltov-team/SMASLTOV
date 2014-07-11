package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

public class UnaryPrimitiveExpressionAST extends ExpressionAST {
    private int type;
    private Symbol var;

    /** Constructor */
    public UnaryPrimitiveExpressionAST(int type, Symbol var){
	this.type = type;
	this.var = var;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){}
    public void substitute(List froms, List tos){}

    /** UnaryPrimitive */
     public AST copy(){
	 return this;
    }

    /** Evaluate Foreach intructions */
    public void evaluate(){}

    /** Generate an Expression */
    public Expression getExpression(){
	return new UnaryPrimitiveExpression(type,var);
    }
}
