package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class TCFormulaAST extends FormulaAST {
	Var leftSub;
	Var rightSub;
	Var left;
	Var right;
	FormulaAST subFormula;

	public TCFormulaAST(Var left, Var right, Var leftSub, Var rightSub,
						FormulaAST subFormula) {
		this.type = "TransitiveFormula";
		this.leftSub = leftSub;
		this.rightSub = rightSub;
		this.left = left;
		this.right = right;
		this.subFormula = subFormula;
	}

	public AST copy() {
		return new TCFormulaAST(left, right, leftSub, rightSub, (FormulaAST) subFormula.copy());
	}

	public void substitute(String from, String to) {
		subFormula.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
		subFormula.substitute(froms, tos);
	}

	public Formula getFormula() {
		return new TransitiveFormula(left, right,
									 leftSub, rightSub,
									 subFormula.getFormula());
	}
}
