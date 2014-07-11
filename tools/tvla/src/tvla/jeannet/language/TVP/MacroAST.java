package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;

public class MacroAST extends AST {
	public AST copy() {
		throw new RuntimeException("Can't copy macro definitions.");
	}

	public void substitute(String from, String to) {
		throw new RuntimeException("Can't substitute macro definitions.");
	}
	public void substitute(List froms, List tos) {
		throw new RuntimeException("Can't substitute macro definitions.");
	}
}
