package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;

/** An abstract syntax node for an equality formula.
 * @author Tal Lev-Ami.
 */
public class EqualityAST extends FormulaAST {
	Var left;
	Var right;

	public EqualityAST(Var left, Var right) {
		this.type = "EqualityFormula";
		this.left = left;
		this.right = right;
	}

	public AST copy() {
		// No need to copy;
		return this;
	}

	public void substitute(String from, String to) {
		// Do nothing.
	}
    public void substitute(List froms, List tos){
		// Do nothing.
	}

	public Formula getFormula() {
		return new EqualityFormula(left, right);
	}
}
