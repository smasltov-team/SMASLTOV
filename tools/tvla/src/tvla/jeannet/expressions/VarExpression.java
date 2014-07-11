package tvla.jeannet.expressions;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.formulae.*;

/** A class for variable expression
 * @author Bertrand Jeannet
 */

public class VarExpression implements Expression {
    Symbol var;

    public String toString(){
	return var.toString();
    }

    public VarExpression(Symbol var){
	this.var = var;
    }
    
    public boolean hasValue(){ return true; }
    
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime){
	return bindings.lookup(var);
    }
}    
