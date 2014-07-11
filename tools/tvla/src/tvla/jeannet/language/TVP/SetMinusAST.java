package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class SetMinusAST extends SetAST {
	SetAST left;
	SetAST right;

	public SetMinusAST(SetAST left, SetAST right) {
		this.left = left;
		this.right = right;
	}

	public Set getMembers() {
		Set leftMembers = left.getMembers();
		Set rightMembers = right.getMembers();
		for (Iterator idIt = rightMembers.iterator(); idIt.hasNext(); ) {
			String id = (String) idIt.next();
			leftMembers.remove(id);
		}
		return leftMembers;
	}

	public AST copy() {
		return new SetMinusAST((SetAST) left.copy(), (SetAST) right.copy());
	}

	public void substitute(String from, String to) {
		left.substitute(from, to);
		right.substitute(from, to);
	}
	public void substitute(List froms, List tos) {
		left.substitute(froms, tos);
		right.substitute(froms, tos);
	}
}
