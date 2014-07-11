package tvla.jeannet.equationSystem;

import tvla.core.*;
import tvla.core.assignments.*;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.logic.*;
import tvla.predicates.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import java.util.*;

/** This class represents a function appearing in the system of fixpoint equations
 * @author Bertrand Jeannet
 */

public class Equation {
    protected Symbol result;
    protected Function func;
    protected List args; // List of objects of type Symbol
 
    public Equation(Symbol result, Function func, List args){
	this.result = result;
	this.func = func;
	this.args = args;
    }
    public Function getFunction(){
	return func;
    }
    public List getArguments(){
	return args;
    }
    public Symbol getResult(){
	return result;
    }
}

