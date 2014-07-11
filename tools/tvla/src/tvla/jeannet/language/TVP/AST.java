package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;

/** The base class for all abstract syntax tree nodes.
 * @author Tal Lev-Ami.
 */
public abstract class AST {
	public abstract AST copy();

	public abstract void substitute(String from, String to);
	public abstract void substitute(List froms, List tos);

	public void generate() {
	}

	public static List copyList(List orig) {
		List result = new ArrayList();
		for (Iterator j = orig.iterator(); j.hasNext(); ) {
			AST ast = (AST) j.next();
			result.add(ast.copy());
		}
		return result;
	}

	/** Substitution of names. */
	public static void substituteList(List orig, String from, String to) {
		for (Iterator j = orig.iterator(); j.hasNext(); ) {
			AST ast = (AST) j.next();
			ast.substitute(from, to);
		}
	}
	/** Parallel substitution of names.  Added by Bertrand: macro
	    expansion requires PARALLEL substitution. */
	public static void substituteList(List orig, List froms, List tos) {
		for (Iterator j = orig.iterator(); j.hasNext(); ) {
			AST ast = (AST) j.next();
			ast.substitute(froms, tos);
		}
	}
}
