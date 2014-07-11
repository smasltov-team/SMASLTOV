package tvla.jeannet.expressions;

import tvla.exceptions.*;
import tvla.util.*;
import java.util.*;

public class SeqExpression implements Expression {
    List exprs;

    /** Constructor */
    public SeqExpression(List exprs) {
	this.exprs = new ArrayList(exprs);
	// Checks that all but the last expressions are side effects
	// instructions
	int j=0;
	for (Iterator i = exprs.iterator(); i.hasNext(); ){
	    Expression expr = (Expression)i.next();
	    j++;
	    if (i.hasNext()){
		if (expr.hasValue()){
		    throw new UserErrorException("In a sequence \"begin ... end\", the " + j + "th expression returns a value, but it should not");
		}
	    } 
	    else {
		if (! expr.hasValue()){
		    throw new UserErrorException("In a sequence \"begin ... end\", the " + j + "th (last) expression does not return a value, but it should");
		}
	    }
	}
    }
    /** Accessor */
    public List getExprs(){ return exprs; }

    /** Does the expression has a value ? */
    public boolean hasValue(){ 
	Expression last = (Expression)exprs.get(exprs.size()-1);
	return last.hasValue();
    }

    /** Conversion to String. */
    public String toString(){
	return exprs.toString();
    }

    /** Evaluate the expression on the given environment */
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime) {
	ValueExpression result = null;
	for (Iterator i = exprs.iterator(); i.hasNext(); ){
	    Expression expr = (Expression)i.next();
	    result = expr.eval(bindings, runtime);
	    if (i.hasNext() && result != null)
		throw new UserErrorException("In a sequence, instruction expected, instead of an expression\n" + this);
	}
	if (result==null)
	    throw new UserErrorException("At the end of a sequence, expression expected, instead of an instruction\n");

	return result;
    }
}


