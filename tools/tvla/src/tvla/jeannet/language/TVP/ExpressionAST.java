package tvla.jeannet.language.TVP;

import tvla.logic.*;
import tvla.jeannet.expressions.*;
import java.util.*;

/** The base class of all abstract syntax tree nodes used to represent formulae.
 * @author Bertrand Jeannet.
 */
public abstract class ExpressionAST extends AST {
    public abstract AST copy();
    public abstract void evaluate();  // Evaluate Foreach instructions
    public abstract Expression getExpression();
}
