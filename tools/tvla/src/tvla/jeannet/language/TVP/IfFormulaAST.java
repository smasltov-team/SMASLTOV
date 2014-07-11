package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;

public class IfFormulaAST extends FormulaAST {
	FormulaAST condSubFormula;
	FormulaAST trueSubFormula;
	FormulaAST falseSubFormula;

	public IfFormulaAST(FormulaAST condSubFormula, FormulaAST trueSubFormula,
						FormulaAST falseSubFormula) {
		this.type = "IfFormula";
		this.condSubFormula = condSubFormula;
		this.trueSubFormula = trueSubFormula;
		this.falseSubFormula = falseSubFormula;
	}

	public AST copy() {
		return new IfFormulaAST((FormulaAST) condSubFormula.copy(),
								(FormulaAST) trueSubFormula.copy(),
								(FormulaAST) falseSubFormula.copy());
	}

	public void substitute(String from, String to) {
		condSubFormula.substitute(from, to);
		trueSubFormula.substitute(from, to);
		falseSubFormula.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
		condSubFormula.substitute(froms, tos);
		trueSubFormula.substitute(froms, tos);
		falseSubFormula.substitute(froms, tos);
	}

	public Formula getFormula() {
		return new IfFormula(condSubFormula.getFormula(), trueSubFormula.getFormula(),
							 falseSubFormula.getFormula());
	}
}
