package tvla.jeannet.language.TVP;

import tvla.exceptions.*;
import tvla.formulae.*;
import java.util.*;

/** @author Tal Lev-Ami.
 */
public class SetUseAST extends SetAST {
	String setName;

	public SetUseAST(String setName) {
		this.setName = setName;
	}

	public Set getMembers() {
		if (!SetDefAST.allSets.containsKey(setName))
			throw new SemanticErrorException("Unknown set " + setName);
		return new HashSet((Collection) SetDefAST.allSets.get(setName));
	}

	public AST copy() {
		return new SetUseAST(setName);
	}

	public void substitute(String from, String to) {
		if (from.equals(setName)) {
			setName = to;
		}
	}
	public void substitute(List froms, List tos) {
	    for (int i=0; i<froms.size(); i++){
		String from = (String)froms.get(i);
		if (from.equals(setName)) {
		    setName = (String)(tos.get(i));
		    break;
		}
	    }
	}
}
