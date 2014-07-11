package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class SetPlusAST extends SetAST {
	private SetAST left;
	private SetAST right;

	public SetPlusAST(SetAST left, SetAST right) {
		this.left = left;
		this.right = right;
	}

	public Set getMembers() {
		Set leftMembers = left.getMembers();
		Set rightMembers = right.getMembers();
		for (Iterator idIt = rightMembers.iterator(); idIt.hasNext(); ) {
			String id = (String) idIt.next();
			leftMembers.add(id);
		}
		return leftMembers;
	}

	public AST copy() {
		return new SetPlusAST((SetAST) left.copy(), (SetAST) right.copy());
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
