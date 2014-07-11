package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.logic.*;
import tvla.formulae.*;

/** An abstract syntax node of a 'foreach' statement.
 * @author Tal Lev-Ami.
 */
public class ForeachAST extends AST {
    String id;
    SetAST set;
    List asts;

    public ForeachAST(String id, SetAST set, List asts) {
	this.id = id;
	this.set = set;
	this.asts = asts;
    }

    public List evaluate() {
	List result = new ArrayList();
	for (Iterator memberIt = set.getMembers().iterator(); memberIt.hasNext(); ) {
	    String member = (String) memberIt.next();
	    for (Iterator astIt = this.copyList(asts).iterator(); astIt.hasNext(); ) {
		AST ast = (AST) astIt.next();
		ast.substitute(id, member);
		if (ast instanceof ForeachAST) {
		    result.addAll(((ForeachAST) ast).evaluate());
		}
		else {
		    result.add(ast);
		}
	    }
	}
	return result;
    }

    public AST copy() {
	return new ForeachAST(id, (SetAST) set.copy(), copyList(asts));
    }

    public void substitute(String from, String to) {
	if (from.equals(id))
	    throw new RuntimeException("Trying to substitute the variable of a foreach (" +
				       id + ")");
	set.substitute(from, to);
	substituteList(asts, from, to);
    }
    public void substitute(List froms, List tos) {
	for (Iterator i=froms.iterator(); i.hasNext(); ){
	    String from = (String)i.next();
	    if (from.equals(id))
		throw new RuntimeException("Trying to substitute the variable of a foreach (" +
					   id + ")");
	}
	set.substitute(froms, tos);
	substituteList(asts, froms, tos);
    }
}
