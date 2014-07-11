package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.formulae.*;
import java.util.*;

public class QuantFormulaAST extends FormulaAST {
	List bound;
	FormulaAST subFormula;

	public QuantFormulaAST(List bound, FormulaAST subFormula, String type) {
		this.type = type;
		this.bound = bound;
		this.subFormula = subFormula;
	}

	public AST copy() {
		return new QuantFormulaAST(bound, (FormulaAST) subFormula.copy(), type);
	}

	public void substitute(String from, String to) {
		subFormula.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
		subFormula.substitute(froms, tos);
	}

	private static Formula buildAllQuant(Iterator iterator, FormulaAST subFormula) {
		if (iterator.hasNext()) {
			Var var = (Var) iterator.next();
			return new AllQuantFormula(var, buildAllQuant(iterator, subFormula));
		} else {
			return subFormula.getFormula();
		}
	}

	private static Formula buildExistQuant(Iterator iterator, FormulaAST subFormula) {
		if (iterator.hasNext()) {
			Var var = (Var) iterator.next();
			return new ExistQuantFormula(var, buildExistQuant(iterator, subFormula));
		} else {
			return subFormula.getFormula();
		}
	}

	public Formula getFormula() {
		if (type.equals("AllQuantFormula")) {
			return buildAllQuant(bound.iterator(), subFormula);
		} else if (type.equals("ExistQuantFormula")) {
			return buildExistQuant(bound.iterator(), subFormula);
		} else {
			throw new SemanticErrorException("Formula type (" + type + " unknown.");
		}
	}
}
