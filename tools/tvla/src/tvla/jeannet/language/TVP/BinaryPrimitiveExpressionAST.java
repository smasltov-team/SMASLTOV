package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

/** 
 * @author Bertrand Jeannet
 * @author Jason Breck (added joinwith, diff_over, diff_under)
 */

public class BinaryPrimitiveExpressionAST extends ExpressionAST {
    private int type;
    private Symbol var1;
    private Symbol var2;

    /** Constructor */
    public BinaryPrimitiveExpressionAST(int type, Symbol var1, Symbol var2){
	this.type = type;
	this.var1 = var1;
	this.var2 = var2;
    }

    /** Substitutes predicate names */
    public void substitute(String from, String to){}
    public void substitute(List froms, List tos){}

    /** BinaryPrimitive */
     public AST copy(){
	 return this;
    }

    /** Evaluate Foreach intructions */
    public void evaluate(){}

    /** Generate an Expression */
    public Expression getExpression(){
	return new BinaryPrimitiveExpression(type,var1,var2);
    }
}
