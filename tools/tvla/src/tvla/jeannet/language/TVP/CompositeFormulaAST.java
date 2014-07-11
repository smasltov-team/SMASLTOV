package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.logic.*;
import java.util.*;

public class CompositeFormulaAST extends FormulaAST {
	List subFormulas = new ArrayList();

	public CompositeFormulaAST(FormulaAST subFormula) {
		// Not is the only unary composite
		type = "NotFormula";
		subFormulas.add(subFormula);
	}

	public CompositeFormulaAST(FormulaAST leftSubFormula, FormulaAST rightSubFormula, String type) {
		this.type = type;
		subFormulas.add(leftSubFormula);
		subFormulas.add(rightSubFormula);
	}

	private CompositeFormulaAST(CompositeFormulaAST other) {
		this.subFormulas = copyList(other.subFormulas);
		this.type = other.type;
	}

	public AST copy() {
		return new CompositeFormulaAST(this);
	}

	public void substitute(String from, String to) {
		substituteList(subFormulas, from, to);
	}
	public void substitute(List froms, List tos){
		substituteList(subFormulas, froms, tos);
	}

	public Formula getFormula() {
		if (type.equals("NotFormula")) {
			return new NotFormula(((FormulaAST) subFormulas.get(0)).getFormula());
		}
		else if (type.equals("OrFormula")) {
			return new OrFormula(((FormulaAST) subFormulas.get(0)).getFormula(),
								 ((FormulaAST) subFormulas.get(1)).getFormula());
		}
		else if (type.equals("AndFormula")) {
			return new AndFormula(((FormulaAST) subFormulas.get(0)).getFormula(),
								  ((FormulaAST) subFormulas.get(1)).getFormula());
		}
		else if (type.equals("EquivalenceFormula")) {
			return new EquivalenceFormula(((FormulaAST) subFormulas.get(0)).getFormula(),
										  ((FormulaAST) subFormulas.get(1)).getFormula());
		}
		else {
			throw new SemanticErrorException("Formula type (" + type + " unknown.");
		}
	}
}
