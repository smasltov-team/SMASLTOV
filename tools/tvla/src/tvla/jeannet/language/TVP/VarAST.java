package tvla.jeannet.language.TVP;

import java.util.*;
import tvla.formulae.*;

public class VarAST {
	public static List asVariables(List asStrings) {
		List asVariables = new ArrayList();
		for (Iterator i = asStrings.iterator(); i.hasNext(); ) {
			String str = (String) i.next();
			asVariables.add(new Var(str));
		}
		return asVariables;
	}
}