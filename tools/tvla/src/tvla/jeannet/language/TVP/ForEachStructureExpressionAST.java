package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

public class ForEachStructureExpressionAST extends ExpressionAST {
    static public final int MEET = 1;
    static public final int JOIN = 2;

    private int type;
    private Symbol inputSetVar;
    private Symbol iterVar;
    private ExpressionAST body;

    /** Constructor */
    public ForEachStructureExpressionAST(int type, Symbol inputSetVar, Symbol iterVar, ExpressionAST body){
	this.type = type;
	this.inputSetVar = inputSetVar;
	this.iterVar = iterVar;
	this.body = body;
	switch(this.type) {
	    case MEET :
	    case JOIN :
		break;
            default :
                throw new IllegalArgumentException("ForEachStructureExpressionAST called with bad value for type.");
	}
	if (body == null) {
	    throw new IllegalArgumentException("ForEachStructureExpressionAST called with null body.");
	}
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){
	body.substitute(from, to);
    }
    public void substitute(List froms, List tos){
	body.substitute(froms, tos);
    }

    /** BinaryPrimitive */
    public AST copy(){
	 //return this;
	 return new ForEachStructureExpressionAST(type, inputSetVar, iterVar, (ExpressionAST)body.copy());
    }

    /** Evaluate Foreach intructions */
    public void evaluate(){}

    /** Generate an Expression */
    public Expression getExpression(){
	return new ForEachStructureExpression(type, inputSetVar, iterVar, body.getExpression());
    }
}
