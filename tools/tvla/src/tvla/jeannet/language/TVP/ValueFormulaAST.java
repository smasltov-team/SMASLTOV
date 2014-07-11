package tvla.jeannet.language.TVP;

import tvla.logic.*;
import tvla.core.*;
import tvla.formulae.*;
import java.util.*;

public class ValueFormulaAST extends FormulaAST {
	Kleene value;

	public ValueFormulaAST(Kleene value) {
		this.type = "ValueFormula";
		this.value = value;
	}

	public AST copy() {
		// No need to copy;
		return this;
	}

	public void substitute(String from, String to) {
		// Do nothing.
	}
	public void substitute(List froms, List tos) {
		// Do nothing.
	}

	public Formula getFormula() {
		return new ValueFormula(value);
	}
}
