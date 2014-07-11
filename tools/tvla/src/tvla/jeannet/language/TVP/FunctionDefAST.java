package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.jeannet.equationSystem.*;
import tvla.jeannet.expressions.*;
import tvla.exceptions.*;
import tvla.jeannet.util.Symbol;
import tvla.util.*;

/** An abstract syntax node for an action definition (the action body).
 * @author Tal Lev-Ami.
 */
public class FunctionDefAST extends AST {
    protected MessageAST title;
    protected List parameters;
    protected ExpressionAST body;

    public FunctionDefAST(MessageAST title, List parameters, ExpressionAST body) {
	this.title = title;
	this.parameters = parameters;
	this.body = body;
    }

    public AST copy() {
	return new FunctionDefAST((MessageAST) title.copy(),
				  parameters, // No need for copying, no side effects here
				  (ExpressionAST)body.copy());
    }

    public void substitute(String from, String to) {
	title.substitute(from, to);
	body.substitute(from,to);
    }
    public void substitute(List froms, List tos) {
	title.substitute(froms, tos);
	body.substitute(froms,tos);
    }

    // Evaluate Foreach instructions
    public void evaluate() {
	body.evaluate();
   }

    // Build a Function this object
    public Function getFunction() {
	try {
	    List nparameters = new ArrayList(parameters.size());
	    for (Iterator i=parameters.iterator(); i.hasNext(); ){
		nparameters.add((Symbol)i.next());
	    }
	    return new Function(title.getMessage(),
				nparameters,
				body.getExpression());
	}
	catch (TVLAException e){
	    Logger.println("\nIn the definition of function labeled by " + title.getMessage() + ":");
	    throw e;
	}
    }
}
