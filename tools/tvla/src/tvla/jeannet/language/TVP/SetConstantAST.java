package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

/** An abstract syntax node for sets containing user-specified elements.
 *  @author Tal Lev-Ami.
 */
public class SetConstantAST extends SetAST {
	Set ids;

	public SetConstantAST(Collection ids) {
		this.ids = new HashSet(ids);
	}

	public Set getMembers() {
		return new HashSet(ids);
	}

	public AST copy() {
		return new SetConstantAST(ids);
	}

	public void substitute(String from, String to) {
	    if (ids.remove(from))
		ids.add(to);
	}
	public void substitute(List froms, List tos) {
	    Set nids = new HashSet();
	    for (Iterator i=ids.iterator(); i.hasNext(); ){
		String id = (String)i.next();
		String nid = id;
		for (int k=0; k<froms.size(); k++){
		    if (id.equals((String)froms.get(k))){
			nid = (String)tos.get(k);
			break;
		    }
		}
		nids.add(nid);
	    }
	    this.ids = nids;
	}
}
