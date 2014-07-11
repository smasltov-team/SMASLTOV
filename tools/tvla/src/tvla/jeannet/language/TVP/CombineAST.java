package tvla.jeannet.language.TVP;

import tvla.logic.*;
import tvla.formulae.*;
import java.util.*;

/** An abstract syntax node for a combination of formulae.
 * @author Tal Lev-Ami.
 */
public class CombineAST extends FormulaAST {
	String id;
	SetAST set;
	FormulaAST formula;
	String operator;

	public CombineAST(String operator, FormulaAST formula, String id, SetAST set) {
		this.id = id;
		this.set = set;
		this.operator = operator;
		this.formula = formula;
	}

	public Formula getFormula() {
		FormulaAST result = null;
		for (Iterator memberIt = set.getMembers().iterator(); memberIt.hasNext(); ) {
			String member = (String) memberIt.next();
			FormulaAST term = (FormulaAST) formula.copy();
			term.substitute(id, member);
			if (result == null)
				result = term;
			else
				result = new CompositeFormulaAST(result, term, operator);
		}
		if (result == null) {
			return operator.equals("OrFormula") ?
				   new ValueFormula(Kleene.falseKleene) :
				   new ValueFormula(Kleene.trueKleene);
		}
		return result.getFormula();
	}

	public AST copy() {
		return new CombineAST(operator, (FormulaAST) formula.copy(), id, (SetAST) set.copy());
	}

	public void substitute(String from, String to) {
		if (from.equals(id))
			throw new RuntimeException("Trying to substitute the variable of a foreach (" +
				id + ")");
		set.substitute(from, to);
		formula.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
	    for (Iterator i = froms.iterator(); i.hasNext();){
		String from = (String)i.next();
		if (from.equals(id))
			throw new RuntimeException("Trying to substitute the variable of a foreach (" +
				id + ")");
	    }
	    set.substitute(froms, tos);
	    formula.substitute(froms, tos);
	}
}
