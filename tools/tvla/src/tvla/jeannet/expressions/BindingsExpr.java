package tvla.jeannet.expressions;

import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.exceptions.*;
import java.util.*;

/** A class for environments binding variables to values.
 * @author Bertrand Jeannet
 */

public class BindingsExpr {
    protected Symbol var;
    protected ValueExpression value;
    protected BindingsExpr next;

    public void dump(){
	System.out.println("Dumping BindingsExpr");
	for (BindingsExpr list = this; list != null; list = list.next){
	    System.out.println(list.var.toString() + " size=" + list.value.getCollection().size());
	}
    }

    public BindingsExpr(Symbol var, ValueExpression value, BindingsExpr old){
	this.var = var;
	this.value = value;
	this.next = old;
    }

    /** Looking up the value binded to the variable */
    public ValueExpression lookup(Symbol lvar){
	for (BindingsExpr list = this; list != null; list = list.next){
	    if (list.var.equals(lvar)){
		return list.value;
	    }
	}
	throw (new TVLAException("Unbound variable " + lvar + " in environment of an expression"));
   }
    /** Update the value attached to the variable */
    public void update(Symbol uvar, ValueExpression uvalue){
	for (BindingsExpr list = this; list != null; list = list.next){
	    if (list.var.equals(uvar)){
		list.value = uvalue;
		return;
	    }
	}
	throw (new TVLAException("Unbound variable " + uvar + " in environment of an expression"));
    }
}
