package tvla.jeannet.expressions;

import tvla.exceptions.*;

/** Exception for expressions typing errors.
 * Raised with the expected type, no reference to the erroneous expression at this level
 * @author Bertrand Jeannet
 */
public class ExceptionExpr extends TVLAException {
    public ExceptionExpr(String message) {
	super(message);
    }
}
