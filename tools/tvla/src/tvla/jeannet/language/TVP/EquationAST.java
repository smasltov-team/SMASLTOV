package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.jeannet.expressions.*;
import tvla.jeannet.equationSystem.*;

/** An abstract syntax class for equations
 * @author Bertrand Jeannet
 */

public class EquationAST extends AST {
    protected Symbol result;
    protected FunctionDefAST def;
    protected List args; // List of objects of type Symbol

    public EquationAST(Symbol result, FunctionDefAST def, List args){
	this.result = result;
	this.def = def;
	this.args = args;
    }

    public AST copy(){
	return new EquationAST(result, (FunctionDefAST)def.copy(), new ArrayList(args));
    }

    public void substitute(String from, String to) {
	def.substitute(from, to);
    }
    public void substitute(List froms, List tos) {
	def.substitute(froms, tos);
    }

    public Equation getEquation(){
	return new Equation(result, def.getFunction(), args);
    }
}
