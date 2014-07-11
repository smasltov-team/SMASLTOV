package tvla.jeannet.expressions;

import tvla.exceptions.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

public class LetExpression implements Expression {
    Symbol var;
    Expression expr1;
    Expression expr2;

    /** Constructor */
    public LetExpression(Symbol v, Expression expr1, Expression expr2) {
	this.var = v;
	this.expr1 = expr1;
	this.expr2 = expr2;
	// Checks that expr1 returns a value
	if (! expr1.hasValue()){
	    throw (new UserErrorException("In an expression \"Let " + var + " = e1 in e2\", e1 does not return a value"));
	}
    }

    /** Accessors */
    public Expression getBindingExpr(){ return expr1; }
    public Expression getBodyExpr(){ return expr2; }

    /** Does the expression has a value ? */
    public boolean hasValue(){
	return expr2.hasValue();
    }

    /** Evaluate the expression on the given environment */
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime) {
	ValueExpression v = expr1.eval(bindings,runtime);
	BindingsExpr nbindings = new BindingsExpr(this.var,v,bindings);
	return this.expr2.eval(nbindings,runtime);
    }
}
