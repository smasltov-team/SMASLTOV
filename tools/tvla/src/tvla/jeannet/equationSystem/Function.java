package tvla.jeannet.equationSystem;

import tvla.core.*;
import tvla.exceptions.*;
import tvla.jeannet.expressions.*;
import tvla.jeannet.util.Symbol;
import tvla.util.*;
import java.util.*;

/** This class represents a function appearing in the system of fixpoint equations
 * @author Bertrand Jeannet
 */

public class Function {
    private String title;
    private ArrayList parameters; // Of type Symbol
    private Expression body;

    public Function(String title, List parameters, Expression body){
	this.title = title;
	this.parameters = new ArrayList(parameters);
	this.body = body;
    }

    public String getTitle(){ return title; }
    public List getParameters(){ return parameters; }
    public Expression getBody(){ return body; }

    /** actuals is supposed to be a List of type ValueExpression. */
    public ValueExpression eval(List actuals, RuntimeExpr runtime){
	runtime.primitiveNumber = 0;

	// Creates the environment
	BindingsExpr bindings = null;
	for (int i=0; i<actuals.size(); i++){
	    bindings = new BindingsExpr((Symbol)parameters.get(i),(ValueExpression)actuals.get(i),bindings);
	}
	return body.eval(bindings,runtime);
    }
}
