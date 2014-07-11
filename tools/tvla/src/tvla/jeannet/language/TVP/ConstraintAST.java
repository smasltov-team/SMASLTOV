package tvla.jeannet.language.TVP;

import tvla.core.*;
import tvla.formulae.*;
import java.util.*;

/** An abstract syntax node for a user-defined constraint.
 * @author Tal Lev-Ami.
 */
public class ConstraintAST extends AST {
	FormulaAST body;
	FormulaAST head;

	public ConstraintAST(FormulaAST body, FormulaAST head) {
		this.body = body;
		this.head = head;
	}

	public AST copy() {
		return new ConstraintAST((FormulaAST) body.copy(), (FormulaAST) head.copy());
	}

	public void substitute(String from, String to) {
		body.substitute(from, to);
		head.substitute(from, to);
	}
	public void substitute(List froms, List tos){
		body.substitute(froms, tos);
		head.substitute(froms, tos);
	}

    public void generate() {
	Constraints.getInstance().addConstraint(body.getFormula(), head.getFormula());
    }
}
