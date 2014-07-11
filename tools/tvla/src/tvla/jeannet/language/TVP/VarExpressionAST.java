package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.jeannet.expressions.*;

public class VarExpressionAST extends ExpressionAST {
    private Symbol var;

    /** Constructor */
    public VarExpressionAST(Symbol var){
	this.var = var;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){}
    public void substitute(List froms, List tos){}

    /** Copy */
    public AST copy(){ return this; }

    /** Evaluate Foreach intructions */
    public void evaluate(){}

    /** Generate an Expression */
    public Expression getExpression(){
	return new VarExpression(this.var);
    }
}
