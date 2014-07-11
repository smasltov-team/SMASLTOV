package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

/** An abstract-syntax class for a set defined by the user.
 * @author Tal Lev-Ami.
 */
public class SetDefAST extends AST {
	/** Maps set names to the lists of set members.
	 */
	public static Map allSets = new HashMap();

	/** A label identifying the set.
	 */
	protected String name;

	/** The strings representing the set members.
	 */
	protected List members;

	public SetDefAST(String name, List members) {
		this.name = name;
		this.members = members;

		allSets.put(name, members);
	}

	public AST copy() {
		throw new RuntimeException("Can't copy set definitions.");
	}

	public void substitute(String from, String to) {
		throw new RuntimeException("Can't substitute set definitions.");
	}
	public void substitute(List froms, List tos) {
		throw new RuntimeException("Can't substitute set definitions.");
	}

	public SetDefAST getSet(String name) {
		return (SetDefAST) allSets.get(name);
	}
}
