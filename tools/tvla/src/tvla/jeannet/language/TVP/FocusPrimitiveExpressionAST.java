package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.util.*;
import tvla.jeannet.util.Symbol;
import tvla.jeannet.expressions.*;
import tvla.formulae.*;

/** An class for the Focus primitive
 * @author Bertrand Jeannet
 */

public class FocusPrimitiveExpressionAST extends ExpressionAST {
    private Symbol var;
    private List formulae = new ArrayList();

    /** Constructor */
   public FocusPrimitiveExpressionAST(Symbol var, List formulae){
	this.var = var;
	this.formulae = formulae;
    }
    /** Substitutes predicate names */
    public void substitute(String from, String to){
	substituteList(formulae,from,to);
    }
     public void substitute(List froms, List tos){
	substituteList(formulae,froms,tos);
    }
    /** Copy */
    public AST copy(){
	return new FocusPrimitiveExpressionAST(var, copyList(formulae));
    }
    /** Evaluate Foreach intructions */
    public void evaluate(){
	List newFormulae = new ArrayList();
	for (Iterator i = this.formulae.iterator(); i.hasNext(); ) {
	    AST formula = (AST)i.next();
	    if (formula instanceof ForeachAST) {
		newFormulae.addAll(((ForeachAST) formula).evaluate());
	    } else {
		newFormulae.add(formula);
	    }
	}
	this.formulae = newFormulae;
    }

    /** Generate an Expression */
    public Expression getExpression(){
	List newFormulae = new ArrayList(formulae.size());
	for (Iterator i=formulae.iterator(); i.hasNext(); ){
	    FormulaAST ast = (FormulaAST)i.next();
	    newFormulae.add(ast.getFormula());
	}
	return new FocusPrimitiveExpression(var, newFormulae);
    }

}
