package tvla.jeannet.expressions;

import java.util.*;
import tvla.util.*;

/** An interface for the recursive structure 
 * representing expressions used in function definition
 * @author Bertrand Jeannet
 */
public interface Expression {
    /** Does the expression has a value ? */
    public boolean hasValue();

    /** Evaluate the expression on the given environement, mapping
     * variables to values. Returns a null pointer if the expression
     * does not evaluate to a value, but instead works by
     * side-effect. */
    public ValueExpression eval(BindingsExpr bindings, RuntimeExpr runtime);
}
