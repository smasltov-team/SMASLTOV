package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.logic.*;
import tvla.formulae.*;
import java.util.*;

/** An abstract syntax node for an action macro (an action instantiation).
 * @author Tal Lev-Ami.
 */
public class FunctionMacroAST extends MacroAST {
    protected String name;
    protected List args;
    protected FunctionDefAST def;

    protected static Map functionMacros = new HashMap();

    public FunctionMacroAST(String name, List args, FunctionDefAST def) {
	this.name = name;
	this.args = args;
	this.def = def;
	functionMacros.put(name, this);
    }

    public static FunctionMacroAST get(String name) {
	FunctionMacroAST macro = (FunctionMacroAST) functionMacros.get(name);
	if (macro == null)
	    throw new SemanticErrorException("Unknown macro " + name);
	return macro;
    }

    public FunctionDefAST expand(List actualArgs) {
	if (actualArgs.size() != args.size()) {
	    throw new RuntimeException("For action " + name + " need " +
				       args.size() + " args, but got " +
				       actualArgs.size());
	}
	FunctionDefAST newDef = (FunctionDefAST) def.copy();
	newDef.substitute(args, actualArgs);
	newDef.evaluate();
	return newDef;
    }
}
