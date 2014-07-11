package tvla.jeannet.language.TVP;

import tvla.logic.*;
import tvla.formulae.*;
import java.util.*;

/** The base class of all abstract syntax tree nodes used to represent formulae.
 * @author Tal Lev-Ami.
 */
public abstract class FormulaAST extends AST {
    protected String type;

	public abstract Formula getFormula();
}
